---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: default
---

Welcome to RaccoonForLemmy's homepage!

- [project overview](#overview)
- [rationale](#rationale)
- [key app features](#key-app-features)
- [further reading](#further-reading)

# Overview

Raccoon For Lemmy is a mobile client for Lemmy, a federated aggregation and discussion platform. The
app is implemented as a Kotlin Multiplatform (KMP) project, mainly focusing on the Android platform.

The project was started as an exercise to play around with Kotlin Multiplatform and Compose
multiplatform and gradually grew as a fully functional client with several features.

# Rationale

There are several reasons why this project was started:

- experimenting and making some stress tests with a real world project Kotlin Multiplatform (KMP)
  and Compose multiplatform, in order to explore what could be achieved with multiplatform libraries
  and share as much code as possible in the `commonMain` source directory;
- offering a feature rich Lemmy client mainly aimed at "pro" users, i.e. users who
  are not content with just browsing the contents of the Fediverse, creating posts and
  answers but be able to customize the app and "feel at home" because of its theme, its localization
  and its behaviour that each user has chosen;
- digging deeper inside Lemmy's internals and understand better what it is like to work with a
  decentralized platform; we believe that the Lemmy project has a lot of potential and part of its
  success depends on users being able to enjoy the experience on robust and well-done clients.

In the Lemmy ecosystem for Android, there are many cross-platform alternatives either implemented
with Flutter or React, a couple of native options written in Java and just one native pure-Kotlin
application. We️ ❤️ Kotlin, we ❤️ Free and Open Source Software and we ❤️ native app development, so
there was a niche to be filled and we are trying our best to do so.

Another important factor which determined the creation of a new client was that different projects
had very interesting features but no one had them all at the same time. Lemmur first and Liftoff
then were great apps with a lot of advanced features (e.g. the possibility to explore all the
communities of an external instance in guest mode). Summit is another great project with unique
features like multi-communities, a high degree of customization and very good performance. Finally,
Thunder has a really appealing and polished UI. All these apps have been and continue to be sources
of inspiration and motivation for Raccoon, which aims at having all the good sides of them, in a
Kotlin open source app.

If you are wondering why the app is called like this, aren't raccoons adorable? 🦝 Jokes aside,
Lemmy users used to refer to themselves as "lemmings" and the platform logo clearly recalls that
animal, so there was this tradition in the early days to use animals names for clients (some
examples being Lemmur, Jerboa and Fennec). The original developers of this app really liked raccoons
so they choose that. Raccoons are cunning animals that adapt to live in
harsh conditions, finding a way to survive even with what we consider "trash". They look like masked
criminals but do not harm anyone, they are lazy and chubby and so cute.

As long as you are on this page, don't forget every raccoon's motto: «Live Fast, Eat Trash»
(abbreviated L.F.E.T.).

# Key app features

Here is a list of the most important features of the app:

- view post feed and comments with different listing and sort types;
- possibility to upvote and downvote (with optional and configurable swipe actions);
- community and user detail (with info about moderators/moderated communities);
- user profile with one's own posts, comments and saved items;
- inbox with replies, mentions and direct messages in the form of a chat;
- full-fledged explore section with different result types (all, posts, comments, user,
  communities);
- create and edit new posts (with optional images);
- cross-post contents to other communities;
- reply to post and comments (and edit replies);
- mark posts as read (even while scrolling) and hide read contents;
- seeing raw post/comment source;
- custom appearance (color scheme, fonts, text sizes, post layout, etc.);
- custom localization (independent of system settings);
- block users, communities and instances (the latter requires Lemmy >= 0.19);
- report post and comments to moderators;
- support for multiple accounts (and multiple instances) with account-specific settings;
- lazy scrolling (referred to as "zombie mode");
- explore all the communities on a given instance in guest mode (instance info);
- multi-community (community aggregation);
- moderation log and the possibility to create reports;
- community moderation tool (examine and resolve reports, ban users, feature posts, block
  further comments from posts, mark comments as distinguished, remove posts/comments).

Most clients for Lemmy currently offer the first points (with various degrees of completion),
whereas the last ones are trickier and less common, so they are directed to more picky users.

I like to be able to customize the appearance of my apps, so the ability to change font face or size
and colors was of paramount importance to me. Similarly, I like when I can use an app in my native
language and change the UI language independently from the system language, so localization is a
first-class citizen in this project too.

# Further reading

If what you have read so far sounds interesting and you want to know more, here are some useful
links:

- view the project on GitHub and have a look at
  the [CONTRIBUTING.md](https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/CONTRIBUTING.md)
- se the [Matrix space](https://matrix.to/#/#raccoonforlemmy:matrix.org) to chat in real time with
  other team members, there are two rooms in the space: "General" is for general information about
  the app development and "Trashcan" is more for smalltalk and random topics.
- use the [Lemmy community](https://lemmy.world/c/raccoonforlemmy) to receive updates about the new
  releases, participate into public discussions in the Lemmy style and provide your feedback or even
  share your memes about raccoons with any other interested people
- use the [GitHub issue tracker](https://github.com/diegoberaldin/RaccoonForLemmy/issues) to report
  bugs or request features
- use the [GitHub discussion section](https://github.com/diegoberaldin/RaccoonForLemmy/discussions)
  for technical questions about the release roadmap, questions about app internationalization, etc.
- finally, if none of the above methods fits your needs you
  can [write an email](mailto:raccoonforlemmy@gmail.com) or send a private
  message to the original developer on Lemmy.