<div align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.21-7f52ff?logo=kotlin" />
  <img src="https://img.shields.io/badge/Android-26+-green" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-1.5.11-3e7fea?logo=jetpackcompose" />
  <img src="https://img.shields.io/github/license/diegoberaldin/RaccoonForLemmy" />
</div>

<br />

<div align="center">
  <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/6785188f-9c2a-4622-ab6b-5aa116d27c31" width="250" height="auto" />
</div>

# Raccoon for Lemmy

A Kotlin Multiplatform Mobile client for Lemmy.

<div align="center">
<table>
  <tr>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/a41fe45c-b1be-44bb-b044-fd6ef38bc205" width="310" alt="home screen" />
    </td>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/a95bfe7b-fdab-4810-a1be-c6baf3270a51" width="310" alt="post detail" />
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/621ab6ae-a08d-4a2a-a68b-e1c6924325e9" width="310" alt="explore screen" />
    </td>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/93df3d76-efd0-47c8-a135-c7fae8caf9ca" width="310" alt="community detail" />
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/103bd28d-f75e-4faa-8805-7ba21dc4a98d" width="310" alt="inbox screen" />
    </td>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/d1fff835-11a8-47ed-86c7-c04a531f890e" width="310" alt="settings screen" />
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/3529a977-3cb8-4465-a828-ee43bf056f77" width="310" alt="community info" />
    </td>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/b97f7e7f-57ee-4fe5-8f84-386b2c4ac111" width="310" alt="instance info" />
    </td>
  </tr>
</table>
</div>

Raccoon for Lemmy is a client for the federated aggregation and discussion platform Lemmy.

The project started as an exercise to play around with Kotlin Multiplatform (KMP) and Compose
multiplatform and gradually grew as a fully functional client with many features.

## Main features

- view post feed and comments with different listing and sort types;
- possibility to upvote and downvote (with optional swipe actions)
- community and user detail;
- user profile with one's own posts, comments and saved items;
- inbox with replies, mentions and direct messages;
- global search on Lemmy with different result types (all, posts, comments, user, communities);
- create and edit new posts (with optional images);
- reply to post and comments (and edit replies);
- mark posts as read and hide read contents;
- custom appearance (color scheme, fonts, text sizes, post layout, etc.);
- custom localization (independent of system settings);
- block users, communities and instances (the latter requires Lemmy >= 0.19);
- report post and comments to moderators;
- support for multiple accounts (and multiple instances) with account-specific settings;
- lazy scrolling (referred to as "zombie mode");
- explore all the communities on a given instance in guest mode;
- multi-community (community aggregation);
- community moderation tools and moderation log.

Most clients for Lemmy currently offer the first points (with various degrees of completion), so
there is nothing special about Raccoon for Lemmy, whereas the last ones are less common and are
directed to more picky users (like me) who like to explore the Lemmy ecosystem and want to be able
to group feed contents arbitrarily.

I like to be able to customize the appearance of my apps, so the ability to change font face or
size and colors was of paramount importance to me. Similarly, I like when I can use an app in my
native language and change the UI language independently from the system language, so localization
is a first-class citizen in this project too.

If you are a moderator of a community, it is also good to be able to moderate content from your
mobile device instead of using the web interface, but in-app moderation is rare in the existing
clients (and even more so in _open source_ clients). Raccoon For Lemmy is trying to bridge this gap
and offers some extent of moderation tools (feature post, lock post, distinguish comment, remove
post/comment, ban users and the ability to revert any of these actions).

The application is under active development, so expect new features to be added over time. Have a
look on the issues labeled with "feature" in the issue tracker to get an idea of what's going to
come next.

If you have ideas, feedback, suggestions or comments remember to speak up and use your
voice. You can add reports or request features and they will be considered.

## Why was the project started?

Because raccoons are so adorable, aren't they? 🦝🦝🦝

Joking apart, one of the main goals was to experiment with KMP and learn how to properly deal
with the challenges of a multiplatform environment, and a medium-sized project like this was an
ideal testing ground for that technology.

Secondly, as a means to dig deeper inside Lemmy's internals and become more humble and patient
towards other apps whenever I found anything lacking or not implemented in an optimal way (according
to my taste).

In the third place, I felt that the Android ecosystem of Lemmy apps was a little "poor" with few
native apps (fewer open source), while the "market" is dominated by iOS and cross platform clients.
I️ ❤️ Kotlin, I ❤️ Free and Open Source Software and I ❤️ native app development, so there was a
"niche" that needed to be filled.

Moreover, developing a new client was an opportunity to add all the features that I needed and that
were "scattered" across different apps, e.g. I liked the feature richness
of [Liftoff](https://github.com/liftoff-app/liftoff) (e.g. the possibility to explore all the
communities of an external instance in guest mode), the multi-community feature of
[Summit](https://github.com/idunnololz/summit-for-lemmy) and the polished UI of
[Thunder](https://github.com/thunder-app/thunder) and I wished I could have them all in the same
app.

This involves a high level of discretionality and personal taste, I know, but this project _is_ all
about experimenting and learning.

## Technologies used:

- [Koin](https://github.com/InsertKoinIO/koin) for dependency injection
- [Voyager](https://github.com/adrielcafe/voyager) for screen navigation
- [Ktor](https://github.com/ktorio/ktor) and [Ktorfit](https://github.com/Foso/Ktorfit) for
  networking in conjunction with kotlinx-serialization for JSON marshalling
- [Moko resources](https://github.com/icerockdev/moko-resources) for resource management
- [Kamel](https://github.com/Kamel-Media/Kamel) for lazy image loading, but later switched to
  [Coil](https://github.com/coil-kt/coil) on Android because there was a major bug with large images
- [Multiplatform settings](https://github.com/russhwolf/multiplatform-settings) for encrypted
  preferences
- [SQLDelight](https://github.com/cashapp/sqldelight)
  and [SQLCipher](https://github.com/sqlcipher/sqlcipher) for local persistence
- for Markdown rendering, the initial approach involved using
  [Markdown](https://github.com/JetBrains/markdown) for parsing in conjunction with custom rendering
  but this approach proved difficult to extend and maintain so on Android the
  [Markwon](https://github.com/noties/Markwon) library was chosen instead.

## Credits:

- the `core-md` module in the iOS flavor is inspired by
  [Multiplatform Markdown Renderer](https://github.com/mikepenz/multiplatform-markdown-renderer) and
  the Android implementation with Markwon is adapted from
  [Jerboa for Lemmy](https://github.com/dessalines/jerboa)
- the UI is inspired by the really great [Thunder](https://github.com/thunder-app/thunder) app

## Acknowledgements:

This project would not be what it is were it not for the huge amount of patience and dedication
of early adopters who sent me continous feedback and ideas for improvement after every release.

A special thank to all those who contributed so far:

- [u/rb_c@discuss.tchncs.de](https://discuss.tchncs.de/u/rb_c)
- [u/heyazorin@lemmy.ml](https://lemmy.ml/u/heyazorin)
- [u/thegiddystitcher@lemm.ee](https://lemm.ee/u/thegiddystitcher)
- [outerair](https://github.com/outerair)
- all those who reported feedback and ideas through the Lemmy community, GitHub issues, emails,
  private messages, homing pidgeons and every other imaginable media.

## Want to try it out?

Here are some options to install the application on your device.

<a href="https://github.com/ImranR98/Obtainium/releases">
  <img src="https://img.shields.io/badge/Install with:-Obtainium-4d29a0" />
</a>

<br />

<a href="https://play.google.com/apps/testing/com.github.diegoberaldin.raccoonforlemmy.android">
  <img src="https://img.shields.io/badge/Install with:-Google_Play-34a853" />
</a>

## Want to leave your feedback or report a bug?

- open an issue on this
  project's [issue tracker](https://github.com/diegoberaldin/RaccoonForLemmy/issues) to report bugs
  or request new features
- create a post on the project's [community](https://lemmy.world/c/raccoonforlemmy) on
  Lemmy.world for broader questions, opinions, personal feedback, suggestions, insults or whatever
  you feel like writing
- join the [Matrix space](https://matrix.to/#/#raccoonforlemmy:matrix.org) dedicated to the project
- if you are a translator and want to help out with l10n or submit some corrections but you don't
  feel confident with repository forks, pull requests, managing resource files, etc. feel free to
  drop an email or contact me in any way.

Please remember: every contribution is welcome and everyone's opinion matters here. This is a
community project, open source, ad-free and free of charge, and it belongs to us all so don't be
afraid to get involved.

And don't forget every 🦝's motto: «Live Fast, Eat Trash» (for shortness L. F. E. T.).
