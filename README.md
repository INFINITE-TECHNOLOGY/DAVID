# Infinite Technology ∞ David 🤖

|Attribute\Release type|Latest|
|----------------------|------|
|Version|0.0.0-SNAPSHOT|
|Branch|[master](https://github.com/INFINITE-TECHNOLOGY/DAVID)|
|CI Build status|[![Build Status](https://travis-ci.com/INFINITE-TECHNOLOGY/DAVID.svg?branch=master)](https://travis-ci.com/INFINITE-TECHNOLOGY/DAVID)|
|Test coverage|[![codecov](https://codecov.io/gh/INFINITE-TECHNOLOGY/DAVID/branch/master/graphs/badge.svg)](https://codecov.io/gh/INFINITE-TECHNOLOGY/DAVID/branch/master/graphs)|
|Library (Maven)|[oss.jfrog.org snapshot](https://oss.jfrog.org/artifactory/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/io/infinite/david/0.0.1-SNAPSHOT)|
|Heroku|![Heroku](https://heroku-badge.herokuapp.com/?app=david-demo&root=/david/unsecured)|

## Purpose

`David` - a Polyglot Telegram Chat Bot.

`David` is based on [AbilityBot](https://github.com/rubenlagus/TelegramBots) adding support of Plugins written in Groovy Script.

## In short

`David` helps to speed-up the development and enrollment of Telegram chat bots by taking care of the following verticals:

- SRE/DevOps
  - Deployment friendly - `David` is Spring Boot Application fully packaged and ready to be deployed anywhere
  - DB friendly - JPA support comes aboard (somehow a pain point of `TelegramBots` users)
  - Cloud-native - `David` provides extendable Push REST API. `David` is an extrovert! He can start the conversation, not only reply.
  - Logging - another pain point of `TelegramBots` users, though improved in latest versions (SLF4J) support. Yet `David` takes it to another level using [BlackBox](https://github.com/INFINITE-TECHNOLOGY/BLACKBOX) - you *won't even have to write logging code* yourself in plugins - it is added automatically.
- Development & built-in features\
    `David` already supports essential routines: 
  - User input handling - requesting and waiting for user input, and gracefully handling wrong data or junk
  - No wasted time on code infrastructure - `David` helps you to go straight to developing the features
  - Breaking your head how to structure your bot code? `David` provides you with the best structure - each command is in separate plugin.
  - User registration - `David` provides user repository with generic KYC attributes
- Business
  - Rapid requirement implementation using scripted language (Groovy)
  - Integration with other [i-t.io](https://i-t.io) products (such as [Pigeon](https://github.com/INFINITE-TECHNOLOGY/PIGEON)) adds value to your product with Email and SMS features
- UX
  - `David` is oriented towards less (or ideally NO) typing by user - thus making it easy to interact with using Telegram buttons rather than text commands.
  - Tired of missed user updates? Can't choose Web Hooks vs Poll? `David` uses **Poll** - the ONLY reliably working mechanism for getting Telegram updates.
- Security
  - `David` comes with JWT and OAuth2 support (via [Ascend](https://github.com/INFINITE-TECHNOLOGY/ASCEND) SECaaS platform)

## Documentation

* [**David Documentation**](https://github.com/INFINITE-TECHNOLOGY/DAVID/wiki)

## Technology stack

* [TelegramBots](https://github.com/rubenlagus/TelegramBots)
* Spring Boot
* Groovy
* SQL DB (via JPA and Spring Data)
* REST+HATEOAS (via Spring Data Rest repositories)
* Bot commands extensible using Plugins (Groovy scripts)

## Try `David` now!

We have deployed a [David Demo Bot](https://github.com/INFINITE-TECHNOLOGY/DAVID_DEMO_PLUGINS) repository is as a Heroku app (`david-demo`).

Just talk to him in Telegram: [@david_demo_bot](https://web.telegram.org/#/im?p=@david_demo_bot)!
