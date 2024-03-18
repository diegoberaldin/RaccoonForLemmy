<div align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?logo=kotlin" />
  <img src="https://img.shields.io/badge/Gradle-8.6-02303A?logo=gradle" />
  <img src="https://img.shields.io/badge/Android-26+-34A853?logo=android" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-1.6.3-4285F4?logo=jetpackcompose" />
  <img src="https://img.shields.io/github/license/diegoberaldin/RaccoonForLemmy" />
</div>

<br />

<div align="center">
  <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/6785188f-9c2a-4622-ab6b-5aa116d27c31" width="240" height="auto" />
</div>

# Raccoon for Lemmy

A Kotlin Multiplatform client for Lemmy (mainly Android).

<div align="center">
  <div style="display: flex; flex-flow: row wrap; justify-content: center; align-items: center;">
    <a href="https://github.com/ImranR98/Obtainium/releases"><img width="200" src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/c341aeee-f779-41f0-b230-0c3081da3de5" /></a>
    <a href="https://play.google.com/store/apps/details?id=com.github.diegoberaldin.raccoonforlemmy.android"><img width="200" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" /></a>
    <a href="https://apt.izzysoft.de/fdroid/index/apk/com.github.diegoberaldin.raccoonforlemmy.android"><img width="200"  src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/5d8c0e3d-552f-488e-b032-987aeadd4c4a" /></a>
  </div>
</div>

Raccoon for Lemmy is a client for the federated aggregation and discussion platform Lemmy.

The project started as an exercise to play around with Kotlin Multiplatform (KMP) and Compose
multiplatform and gradually grew as a fully functional client with many features.

Have a look at the [User manual](https://diegoberaldin.github.io/RaccoonForLemmy/user_manual/main)
for a more detailed explanation or
the [Technical manual](https://diegoberaldin.github.io/RaccoonForLemmy/tech_manual/main)
for some technical notes.

<div align="center">
<table>
  <tr>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/8b05b41f-c338-481c-9ee0-6b440651d04b" width="310" alt="home screen" />
    </td>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/2c5a9ca0-7077-4ca6-9151-5f51588c1343" width="310" alt="post detail" />
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/c326fb31-c5b9-4dc6-8cdc-2faf40d50a82" width="310" alt="community detail" />
    </td>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/30af6355-0312-4d6c-86ac-580229f29979" width="310" alt="explore screen" />
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/4028a389-84b7-44b4-bf8e-71004e529d56" width="310" alt="inbox screen" />
    </td>
    <td>
      <img src="https://github.com/diegoberaldin/RaccoonForLemmy/assets/2738294/223a4c67-7554-454e-99db-56bd840591f8" width="310" alt="settings screen" />
    </td>
  </tr>
</table>
</div>

## Main features

- view post feed and comments with different listing and sort types;
- possibility to upvote and downvote (with configurable swipe actions);
- community and user detail (with info about moderators/moderated communities);
- review your posts and comments (created by you, bookmarked, liked/disliked);
- inbox with replies, mentions and direct messages;
- global search with different result types (all, posts, comments, user, communities);
- create and edit new posts (with optional images);
- cross-post contents to other communities;
- reply to post and comments (and edit replies);
- mark posts as read (even while scrolling) and hide read contents;
- custom appearance (color scheme, fonts, text sizes, post layout, etc.);
- custom localization (independent of system settings);
- block users, communities and instances (the latter requires Lemmy >= 0.19);
- report post and comments to moderators with a reason;
- support for multiple accounts (and multiple instances) with account-specific settings;
- lazy scrolling (referred to as "zombie mode");
- explore all the communities on a given instance in guest mode;
- multi-community (community aggregation);
- view the moderation log;
- community moderation tool (examine and resolve reports, ban users, feature posts, block
  further comments from posts, mark comments as distinguished, remove posts/comments, examine all posts/comments created
  in your communities);
- save posts and comments you are creating as drafts to edit them later;

Most clients for Lemmy currently offer the first points (with various degrees of completion), so
there is nothing special about Raccoon for Lemmy, whereas the last ones are less common and are
directed to more demanding users.

Concerning customization, the ability to change some aspects like font face or size and app colors, vote format, bar
transparency and so on was of paramount importance from the very beginning. Similarly, users should be able to use the
app in their native language and change the UI language independently of the system language.

This app is also intended for moderators who want to use their mobile device, offering moderation tools (feature post,
lock post, distinguish comment, remove post/comment, ban users) and the ability to revert any of these actions.

The project is under active development, so expect new features to be added over time. Have a look on the issues labeled
with "feature" in the issue tracker to get an idea of what's going to come next.

If you have ideas, feedback, suggestions or comments remember to speak up and use your voice. You can add reports or
request features and they will be considered.

## Why was the project started?

Because raccoons are so adorable, aren't they? 🦝🦝🦝

Joking apart, one of the main goals was to experiment with KMP and learn how to properly deal with the challenges of a
multiplatform environment, and a medium-sized project like this was an ideal testing ground for that technology.

Secondly, I felt that the Android ecosystem of Lemmy apps was a little "poor" with few native apps (fewer open source),
while the "market" is dominated by iOS and cross-platform clients. I️ ❤️ Kotlin, I ❤️ Free and Open Source Software and
I ❤️ native app development, so there was a niche that could be filled.

Developing a new client was an opportunity to add all the good features that were "scattered" across different apps,
e.g. the feature richness of [Liftoff](https://github.com/liftoff-app/liftoff), the
multi-community feature of
[Summit](https://github.com/idunnololz/summit-for-lemmy) and the polished UI of the really great
[Thunder](https://github.com/thunder-app/thunder) and so on. This app tries to be configurable
enough to make users feel "at home" and choose what they want, while at the same time having a not
too cluttered interface (except for the Settings screen - I know!)

In the third place, this app has been a means to dig deeper inside Lemmy's internals and become more humble and patient
towards other apps because there are technical difficulties in having to deal with a platform like Lemmy.

This involves a high level of discretion and personal taste, I know, but this project _is_ all about experimenting and
learning.

## Technical notes:

The project uses the following technologies:

- [Koin](https://github.com/InsertKoinIO/koin) for dependency injection
- [Voyager](https://github.com/adrielcafe/voyager) for screen navigation
- [Ktor](https://github.com/ktorio/ktor) and [Ktorfit](https://github.com/Foso/Ktorfit) for networking
- [Lyricist](https://github.com/adrielcafe/lyricist) for l10n
- [Multiplatform settings](https://github.com/russhwolf/multiplatform-settings) for encrypted preferences
- [SQLDelight](https://github.com/cashapp/sqldelight) and [SQLCipher](https://github.com/sqlcipher/sqlcipher) for local
  persistence
- [Multiplatform Markdown Renderer](https://github.com/mikepenz/multiplatform-markdown-renderer) for Markdown
  rendering

More info about the technologies used in the project can be found in
the [CONTRIBUTING.md](https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/CONTRIBUTING.md).

## Want to leave your feedback or report a bug?

- open an issue on this
  project's [issue tracker](https://github.com/diegoberaldin/RaccoonForLemmy/issues) to report bugs
  or request new features
- create a post on the project's [community](https://lemmy.world/c/raccoonforlemmy) on
  Lemmy.world for broader questions, opinions, personal feedback, suggestions, insults or whatever
  you feel like writing
- if you are a translator and want to help out with l10n or submit some corrections but you don't
  feel confident with repository forks, pull requests, managing resource files, etc. feel free to
  drop an email or contact me in any way.

Please remember: every contribution is welcome and everyone's opinion matters here. This is a community project, open
source, ad-free and free of charge, and it belongs to us all so don't be afraid to get involved.

And don't forget every 🦝's motto: «Live Fast, Eat Trash» (abbreviated L.F.E.T.).
