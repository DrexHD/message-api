[![release](https://jitpack.io/v/DrexHD/message-api.svg)](https://jitpack.io/#DrexHD/message-api)

# message-api

A simple server-side message API for Fabric. It allows for translatable messages, with text parsing
using [PlaceholderAPI](https://placeholders.pb4.eu/)
, which can be customized with ease by end users.

## Features

- Special text parsing with PlaceholderAPI
- Easy customizable in-game messages
- Language localization support
- Easy to integrate

## Usage

### Adding the dependency

To use message-api in your mod, add the following to your `build.gradle` file:

Message-api doesn't bundle placeholder-api, you have to bundle a compatible of it as well.
Replace `[TAG]` with the respective version.

```groovy
repositories {
    maven { url = "https://jitpack.io" }
}

dependencies {
    include(modImplementation("com.github.DrexHD:message-api:[TAG]"))
    include(modImplementation("eu.pb4:placeholder-api:[TAG]"))
}
```

### Adding messages

Create a `messages` folder in your resource folder. Create a JSON file with
the [language code](https://minecraft.fandom.com/wiki/Language#Languages) you would like to add, like
the [testmod example](src/testMod/resources/messages/en_us.json).
Add your message to the file (make sure to prefix the message id with your mod id, to avoid conflicts).

### Creating messages

Use any of the provided methods in [LocalizedMessage](src/main/java/me/drex/message/api/LocalizedMessage.java) to get your text instance,
which works like any other vanilla text instance!

```java
ServerPlayerEntity player;
player.sendMessageToClient(LocalizedMessage.localized("modid.some.message.id"));
```

<details>
<summary>Official Mojang Mappings</summary>

```java
ServerPlayer player;
player.sendSystemMessage(LocalizedMessage.localized("modid.some.message.id"));
```

</details>

### User configuration

To change messages, edit the JSON file in `./config/<modid>/<languageid>.json`. This will change the language file for
the mod with the specified `modid`.

## Credits

Special thanks to [PlaceholderAPI](https://placeholders.pb4.eu/) for providing the parsing functionality.
