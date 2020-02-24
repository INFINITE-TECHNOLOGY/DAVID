# Infinite Technology ∞ David 🤖

> Look on my works, ye Mighty, and despair!
- Android `David` from "Alien: Covenant", quoting Percy Shelley's "Ozymandias".

## Purpose

`David` is a ready-to-deploy archetype `Telegram` Chat Bot App, extendable with a user-defined plugins.

## In short

It takes only few clicks to deploy `David` - and you have a functioning (yet empty) chat bot.

Now you can develop your business functionality using `David` Plugins - each one representing a `command`.

The changes you do in the Plugins reflect in the chat immediately - you don't need to re-deploy or restart `David`.

This saves a lot of time and makes development more visualized.

> `David` comes pre-packaged and pre-configured to address typical use cases.

## Features

`David` provides the following additional features:

- Operational
  - Deployment friendly - `David` is Spring Boot Application ready for Cloud deployment (e.g. Heroku)
  - DB friendly - `David` comes with JPA support onboard
  - Logging - another pain point of `TelegramBots` users, though improved in latest versions (SLF4J) support. Yet `David` takes it to another level using [BlackBox](https://github.com/INFINITE-TECHNOLOGY/BLACKBOX) - you *won't even have to write logging code* yourself in plugins - it is added automatically.
- Development & built-in features\
    `David` already supports essential routines: 
  - User input handling - requesting and waiting for user input, and gracefully handling wrong data or junk
  - No wasted time on code infrastructure - `David` helps you to go straight to developing the features
  - Breaking your head how to structure your bot code? `David` provides you with the best structure - each command is in separate plugin.
  - User registration - `David` provides user repository with generic KYC attributes
- Business
  - Rapid requirement implementation using scripted language (Groovy)
  - Push Notifications - `David` provides extendable Push REST API, which help to pro-actively communicate with your bot users
  - Integration with other [i-t.io](https://i-t.io) products (such as [Pigeon](https://github.com/INFINITE-TECHNOLOGY/PIGEON)) adds value to your product with Email and SMS features
- UX
  - `David` is oriented towards less (or ideally NO) typing by user - thus making it easy to interact with using Telegram buttons rather than text commands.
  - Tired of missed user updates? Can't choose Web Hooks vs Poll? `David` uses **Poll** - the ONLY reliably working mechanism for getting Telegram updates.
- Security
  - `David` comes with JWT and OAuth2 support (via [Ascend](https://github.com/INFINITE-TECHNOLOGY/ASCEND) SECaaS platform)
