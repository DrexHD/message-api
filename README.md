[![release](https://jitpack.io/v/DrexHD/message-api.svg)](https://jitpack.io/#DrexHD/message-api)
# message-api [DRAFT]

Message-API is a Minecraft Fabric mod that works on the server-side. It allows for special markdown parsing using [PlaceholderAPI](https://placeholders.pb4.eu/).

With Message-API, users can customize their in-game messages and localize them to their preferred language. This is especially useful for servers with a diverse player base.

## Features

- Special markdown parsing with PlaceholderAPI
- Customizable in-game messages
- Language localization support

## Usage

### Adding the dependency

To use message-api in your mod, add the following to your `build.gradle` file:

```groovy
repositories {
    maven {
        url = "https://jitpack.io"
    }
}

dependencies {
    include(modImplementation("me.drex:message-api:[VERSION]"))
}
```

### Adding messages
Create a messages folder in your resource folder. Create a json file with the [language code](https://minecraft.fandom.com/wiki/Language#Languages) you would like to add, like the [testmod example](src/testmod/resources/messages/en_us.json).
Add your message to the file (make sure to prefix the message id with your mod id, to avoid conflicts). 

### Creating messages
Use any of the provided methods in [Message](src/main/java/me/drex/message/api/Message.java) to get your text instance, which works like any other vanilla text instance!
```java
ServerPlayer player;
player.sendMessageToClient(Message.message("modid.some.message.id"));
```

<details>
<summary>Mojmap</summary>
```java
ServerPlayer player;
player.sendSystemMessage(Message.message("modid.some.message.id"));
```
</details>

### User configuration
To change messages, edit the JSON file in `./config/<modid>/<languageid>.json`. This will change the language file for the mod with the specified `modid`.

## Credits

Special thanks to [PlaceholderAPI](https://placeholders.pb4.eu/) for providing the markdown parsing functionality.
