# Infinite Technology ∞ David 🤖

> Look on my works, ye Mighty, and despair!
- Percy Shelley's "Ozymandias".

## Purpose

`David` is a ready-to-deploy archetype `Telegram` Chat Bot App, extendable with a user-defined plugins.

## In short

It takes only few clicks to deploy `David` - and you have a functioning (yet empty) chat bot.

Now you can develop your business functionality using `David` Plugins - each one representing a `command`.

The changes you do in the Plugins reflect in the chat immediately - you don't need to re-deploy or restart `David`.

This saves a lot of time and makes development more visualized.

> `David` comes pre-packaged and pre-configured to address typical use cases.

## Features

`David` provides the following features:

- Operational
  - Deployment friendly - `David` is Spring Boot Application ready for Cloud deployment (e.g. Heroku)
  - DB friendly - `David` comes with JPA support onboard
  - Logging - SLF4J + [Bobbin](https://github.com/INFINITE-TECHNOLOGY/BOBBIN) + [BlackBox](https://github.com/INFINITE-TECHNOLOGY/BLACKBOX) - the best existing Logging stack. You *won't even have to write logging code* yourself in plugins - it is added automatically.
- Development & built-in features
  - User input handling - requesting and waiting for user input, and gracefully handling wrong data or junk
  - No wasted time on code infrastructure - `David` helps you to go straight to developing the features
  - Code structuring - each bot command is in separate plugin.
  - User registration - `David` provides user repository with generic KYC attributes
  - Step-up authentication - e-mail or SMS OTP via [Pigeon](https://github.com/INFINITE-TECHNOLOGY/PIGEON)
  - Rapid requirement implementation using scripted language (Groovy)
  - Push Notifications - `David` provides extendable Push REST API, which help to pro-actively communicate with your bot users
