<div align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.20-7f52ff?logo=kotlin" />
  <img src="https://img.shields.io/badge/Android-26+-green" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-1.5.10-3e7fea?logo=jetpackcompose" />
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
    <td><!-- Home -->
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/01b3f04b-3900-46dc-b835-efc11c89ab63" width="310" />
    </td>
    <td><!-- Post detail -->
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/c303237d-b5df-40bd-acc6-419aeed8de10" width="310" />
    </td>
  </tr>
  <tr>
    <td><!-- Explore -->
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/e836c063-f85c-488d-b26a-5aa14a49ec2b" width="310" />
    </td>
    <td><!-- Community detail -->
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/e9174244-e0bf-4bcc-bed5-6332e807ce60" width="310" />
    </td>
  </tr>
  <tr>
    <td><!-- Inbox -->
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/1aaff3ac-205b-404d-b83e-ce3f4e267e75" width="310" />
    </td>
    <td><!-- Settings -->
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/3e5e95bf-d09e-4339-bdaf-4e56dedb2bf3" width="310" />
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
- custom appearance (color scheme, font, text size, post layout);
- custom localization (more languages to be added in future versions);
- block users, communities and instances;
- report post and comments to mods;
- support for multiple accounts (and multiple instances) with account-specific settings;
- explore all the communities on a given instance in guest mode;
- multi-community (aggregation).

Most clients for Lemmy currently offer the first points (with various degrees of completion), so
there is nothing special about Raccoon for Lemmy, whereas the last ones are less common and are
directed to more picky users (like me) who like to explore the Lemmy ecosystem and want to be able
to group feed contents arbitrarily. I also like to be able to customize the appearance of my
apps, so the ability to change font face or size and colors was of paramount importance to me.

The application is under active development, so expect new features to be added (e.g. video support,
mod tools, etc.) and the layout is going to change and evolve (hopefully for the better) over time.

## Why was the project started?

Because raccoons are so adorable, aren't they? 🦝🦝🦝

Joking apart, one of the main goals was to experiment with KMP and learn how to properly deal
with the challenges of a multiplatform environment.

Secondly, as a means to dig deeper inside Lemmy's internals and become more humble and patient
towards other apps whenever I found anything lacking or not implemented in an optimal way (according
to my taste).

In the third place, I felt that the Android ecosystem of Lemmy apps was a little "poor" with few
native apps (fewer open source), while the "market" is dominated by iOS and cross platform clients.
I️ ❤️ Kotlin, I ❤️ Free and Open Source Software and I ❤️ native app development, so there was a
"niche" that needed to be filled.

Plus, developing a new client was an opportunity to add all the features that I needed and that
were "scattered" across different apps, e.g. I liked the feature richness
of [Liftoff](https://github.com/liftoff-app/liftoff) (e.g. the possibility to explore all the
communities of an external instance), the multi-community feature of
[Summit](https://github.com/idunnololz/summit-for-lemmy) and the polished UI of
[Thunder](https://github.com/thunder-app/thunder) and I wished I could have them all in the same
app. If I saw a feature of Lemmy (e.g. sorting by "Controversial") that not all apps offer, I could
add it myself, etc. This involves a high level of discretionality and personal taste, I know, but
this project _is_ all about experimenting and learning.

## Technologies used:

- [Koin](https://github.com/InsertKoinIO/koin) for dependency injection
- [Voyager](https://github.com/adrielcafe/voyager) for screen navigation
- [Ktor](https://github.com/ktorio/ktor) and [Ktorfit](https://github.com/Foso/Ktorfit) for
  networking in conjunction with kotlinx-serialization for JSON marshalling
- [Moko resources](https://github.com/icerockdev/moko-resources) for resource management
- [Kamel](https://github.com/Kamel-Media/Kamel) for lazy image loading, but later switched to
  [Coil](https://github.com/coil-kt/coil) on Android because there was a major bug
- [Multiplatform settings](https://github.com/russhwolf/multiplatform-settings) for encrypted
  preferences
- [SQLDelight](https://github.com/cashapp/sqldelight)
  and [SQLCipher](https://github.com/sqlcipher/sqlcipher) for local persistence
- for Markdown rendering, the initial approach involved using
  [Markdown](https://github.com/JetBrains/markdown) for parsing in conjunction with custom rendering
  but this approach proved difficult to extend and maintain so on Android the
  [Markwon](https://github.com/noties/Markwon) library was chosen insted.

## Credits:

- the `core-md` module in the common flavor is heavily inspired by
  [Multiplatform Markdown Renderer](https://github.com/mikepenz/multiplatform-markdown-renderer) but
  the Android implementation with Markwon is adapted from
  [Jerboa for Lemmy](https://github.com/dessalines/jerboa)
- the UI is inspired by the really great [Thunder](https://github.com/thunder-app/thunder) app

## Acknowledgements:

This project would not be what it is were it not for the huge amount of patience and dedication
of early adopters who sent me continous feedback and ideas for improvement after every release.

A special thank to all those who contributed so far, namely:
rb_c, OverfedRaccoon, …

## Want to try it out?

- get it on [Obtainium](https://github.com/ImranR98/Obtainium/releases) by simply adding this
  repo `https://github.com/diegoberaldin/RaccoonForLemmy`

## Want to leave your feedback or report a bug?

- open an issue on this
  project's [issue tracker](https://github.com/diegoberaldin/RaccoonForLemmy/issues) to report bugs
  or request new features
- create a post on the project's [community](https://lemmy.world/c/raccoonforlemmy) on
  Lemmy.world for broader questions, opinions, personal feedback, suggestions, insults or whatever
  you feel like writing
