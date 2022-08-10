# `VirtualEdit`

*Who said Minecraft worlds have to be real?*

**VirtualEdit** is a *library* for Paper that allows developers to display/hide blocks to/from players client-side. It is able to make gigantic visual edits to the world without breaking a sweat.

### VirtualEdit is useful for:
* Mini-game development by allowing for more creative game design...

    - Imagine a parkour that only some players can see
    - Making a wall solid for one player, and completely invisible for another.
* Performing non-destructive edits on the world...

    *Imagine that your neighbour has a very ugly house that you don't want to see.* With VirtualEdit, you can create a plugin that allows players to hide their neighbours' houses without their neighbours even realising. 
* Visualizing builds...

  - Want see how a woodland mansion looks in the nether? You can with VirtualEdit!
  - Maybe you want to see a `200,000` x `200` x `200,000` block of sponge for some reason...
* And more! (I can't be bothered to think of more use cases. Feel free to make a PR with other cool ideas!)

## `Usage`

Just to reiterate. `VirtualEdit` is a *library* designed for **developers**. It is not a plugin that can be used independently on a server. Other developers are free to create plugins that use VirtualEdit for their own use-cases, like the ones mentioned above.

## `Dependencies`

`VirtualEdit` depends on the following:
- Paper (Bukkit and Spigot are not supported)
- ProtocolLib

### `Maven`

#### Add JitPack as a repository

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

#### Add VirtualEdit as a dependency
```xml
<dependency>
  <groupId>com.github.encodeous</groupId>
  <artifactId>VirtualEdit</artifactId>
  <version>master-SNAPSHOT</version>
</dependency>
```