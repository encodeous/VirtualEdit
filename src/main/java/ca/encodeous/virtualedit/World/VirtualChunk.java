package ca.encodeous.virtualedit.World;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.util.Arrays;

import net.imprex.orebfuscator.chunk.*;
import net.imprex.orebfuscator.util.HeightAccessor;

public class VirtualChunk implements AutoCloseable {
    private int chunkX;
    private int chunkZ;
    private HeightAccessor heightAccessor;
    private ChunkSectionHolder[] sections;
    private ByteBuf inputBuffer;
    private ByteBuf outputBuffer;
    private int eBytes;

    public static VirtualChunk fromChunkStruct(ChunkStruct chunkStruct) {
        return new VirtualChunk(chunkStruct, ChunkCapabilities.getExtraBytes(chunkStruct));
    }

    private VirtualChunk(ChunkStruct chunkStruct, int extraBytes) {
        eBytes = extraBytes;
        this.chunkX = chunkStruct.chunkX;
        this.chunkZ = chunkStruct.chunkZ;
        this.heightAccessor = HeightAccessor.get(chunkStruct.world);
        this.sections = new ChunkSectionHolder[this.heightAccessor.getSectionCount()];
        this.inputBuffer = Unpooled.wrappedBuffer(chunkStruct.data);
        this.outputBuffer = PooledByteBufAllocator.DEFAULT.heapBuffer(chunkStruct.data.length);

        for(int sectionIndex = 0; sectionIndex < this.sections.length; ++sectionIndex) {
            if (chunkStruct.sectionMask.get(sectionIndex)) {
                this.sections[sectionIndex] = new ChunkSectionHolder(extraBytes, true);
            }
        }

    }

    public int getSectionCount() {
        return this.sections.length;
    }

    public HeightAccessor getHeightAccessor() {
        return this.heightAccessor;
    }

    public ChunkSection getSection(int index) {
        ChunkSectionHolder chunkSection = this.sections[index];
        return chunkSection != null ? chunkSection.chunkSection : null;
    }

    public ChunkSection createSection(int index) {
        if(getSection(index) != null){
            return getSection(index);
        }
        this.sections[index] = new ChunkSectionHolder(eBytes, false);
        return this.sections[index].chunkSection;
    }

    public int getBlock(int x, int y, int z) {
        if (x >> 4 == this.chunkX && z >> 4 == this.chunkZ) {
            ChunkSectionHolder chunkSection = this.sections[this.heightAccessor.getSectionIndex(y)];
            if (chunkSection != null) {
                return chunkSection.data[positionToIndex(x & 15, y & 15, z & 15)];
            }
        }

        return -1;
    }

    static int positionToIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    public byte[] finalizeOutput() {
        ChunkSectionHolder[] var1 = this.sections;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ChunkSectionHolder chunkSection = var1[var3];
            if (chunkSection != null) {
                chunkSection.write();
            }
        }

        this.outputBuffer.writeBytes(this.inputBuffer);
        return Arrays.copyOfRange(this.outputBuffer.array(), this.outputBuffer.arrayOffset(), this.outputBuffer.arrayOffset() + this.outputBuffer.readableBytes());
    }

    public void close() throws Exception {
        this.inputBuffer.release();
        this.outputBuffer.release();
    }

    private void skipBiomePalettedContainer() {
        int bitsPerValue = this.inputBuffer.readUnsignedByte();
        int dataLength;
        if (bitsPerValue == 0) {
            ByteBufUtil.readVarInt(this.inputBuffer);
        } else if (bitsPerValue <= 3) {
            for(dataLength = ByteBufUtil.readVarInt(this.inputBuffer); dataLength > 0; --dataLength) {
                ByteBufUtil.readVarInt(this.inputBuffer);
            }
        }

        dataLength = ByteBufUtil.readVarInt(this.inputBuffer);
        if (SimpleVarBitBuffer.calculateArraySize(bitsPerValue, 64) != dataLength) {
            throw new IndexOutOfBoundsException("data.length != VarBitBuffer::size " + dataLength + " " + SimpleVarBitBuffer.calculateArraySize(bitsPerValue, 64));
        } else {
            this.inputBuffer.skipBytes(8 * dataLength);
        }
    }

    private class ChunkSectionHolder {
        public ChunkSection chunkSection = new ChunkSection();
        public int[] data;
        public int offset;
        private int extraBytes;

        public ChunkSectionHolder(int extraBytes, boolean readBuffer) {
            if(readBuffer){
                this.data = this.chunkSection.read(inputBuffer);
                this.offset = inputBuffer.readerIndex();
                if (ChunkCapabilities.hasBiomePalettedContainer()) {
                    skipBiomePalettedContainer();
                    this.extraBytes = inputBuffer.readerIndex() - this.offset;
                } else {
                    this.extraBytes = extraBytes;
                    inputBuffer.skipBytes(extraBytes);
                }
            }else{
                ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(2 + 1 + 4 + 4 + 8 * 256);
                buf.clear();
                buf.resetWriterIndex();
                buf.writeShort(1);
                buf.writeByte(0);
                ByteBufUtil.writeVarInt(buf, 1);
                ByteBufUtil.writeVarInt(buf, 0);
                ByteBufUtil.writeVarInt(buf, 256);
                for(int i = 0; i < 256 * 2; i++){
                    buf.writeInt(0);
                }
                buf.resetReaderIndex();
                this.data = this.chunkSection.read(buf);
            }
        }

        public void write() {
            this.chunkSection.write(outputBuffer);
            outputBuffer.writeBytes(inputBuffer, this.offset, this.extraBytes);
        }
    }
}
