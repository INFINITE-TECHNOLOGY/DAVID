# Infinite Technology ∞ David 🤖

> Look on my works, ye Mighty, and despair!
- Percy Shelley's "Ozymandias".

## Download

[ ![Download](https://api.bintray.com/packages/infinite-technology/io.i-t/david-app/images/download.svg) ](https://bintray.com/infinite-technology/io.i-t/david-app/_latestVersion)

## Run

```bash
export ascendClientPrivateKey=<your Private key, you can get one from https://ascend-secaas.herokuapp.com/ascend/public/keyPair
export ascendClientPublicKeyName=<your app name as registered with http://ascend.rest e.g. "MyChatBot">
export ascendGrantingUrl=https://ascend-secaas.herokuapp.com
export ascendValidationUrl=https://ascend-secaas.herokuapp.com
export orbitUrl=https://orbit-secured.herokuapp.com
export botToken="<your Telegram Bot Token>"
export botUsername="<your Telegram Bot user name, as defined with Bot Father>"
export telegramAdminId="<your persaonal Telegram User ID (number), to administrate the bot>"
java -Dserver.port=$PORT $JAVA_OPTS -jar "david-web/build/libs/david-web-1.0.0.jar"
```

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


## Documentation

* [**David Documentation**](https://github.com/INFINITE-TECHNOLOGY/DAVID/wiki)

## Technology stack

* [TelegramBots](https://github.com/rubenlagus/TelegramBots)
* Spring Boot
* Groovy
* SQL DB (via JPA and Spring Data)
* REST+HATEOAS (via Spring Data Rest repositories)
* Bot commands extensible using Plugins (Groovy scripts)
* [Bobbin](https://github.com/INFINITE-TECHNOLOGY/BOBBIN) - logger
* [BlackBox](https://github.com/INFINITE-TECHNOLOGY/BLACKBOX) - logging code automation
* [Ascend](https://github.com/INFINITE-TECHNOLOGY/ASCEND) - step-up authentication & security framework
* [Pigeon](https://github.com/INFINITE-TECHNOLOGY/PIGEON) - e-mail & SMS OTP
* [HTTP Client](https://github.com/INFINITE-TECHNOLOGY/HTTP)

## Try `David` now!

We have deployed a [David Demo Bot](https://github.com/INFINITE-TECHNOLOGY/DAVID_DEMO_PLUGINS) repository is as a Heroku app (`david-demo`).

Just talk to him in Telegram: [@david_demo_bot](https://web.telegram.org/#/im?p=@david_demo_bot)!

Or fork the [repository](https://github.com/INFINITE-TECHNOLOGY/DAVID_DEMO_PLUGINS) and deploy straight to `Heroku`!
