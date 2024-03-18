## Module structure

The project has different kinds of modules and, depending on the group a module belongs to, there are some rules about
which other modules it can depend on.

Here is a description of the dependency flow:

- `:androidApp` which is the KMP equivalent of `:app` module in Android-only projects) include `:shared` and can
  include `:core` modules (e.g. for navigation);
- `:shared` is the heart of the KMP application and it virtually includes every other Gradle module as a dependency (it
  contains in the `DiHelper.kt` files the setup of the DI, so it basically needs to see all Koin modules);
- `:feature` modules are included by :shared and include :domain, :core and :unit modules, but they DO not include other
  each other nor any top level module; some unit modules are used just by one feature (e.g. `:unit:postlist` is used
  only by `:feature:home`) in some other cases multiple features use the same unit (e.g. `:unit:zoomableimage` is used
  by both `:feature:home`, `:feature:search`, `:feature:profile` and `:feature:inbox`);
- `:domain` modules can be used by feature and unit modules and can only include core modules; only exception
  is `:domain:inbox` which is a thin layer on top of `:domain:lemmy` so it depends on it (for inbox related functions);
- `:unit` modules are included by feature modules (and `:shared`) and sometimes by other unit modules in case of highly
  reusable parts of the app; the only notable violation to this rule is `:core:commonui:detailopener-impl` which is a
  special module because it is only included by `:shared` (which does the binding between `:detailopener-api`
  and `:detailopener-impl`) and it includes some unit modules but the fact of a unit module included by a core module in
  general should never happen (instead, the reverse is perfectly ok);
- `:core` modules can sometimes include each other (but without cycles, e.g. `:core:markdown`
  includes `:core:commonui:components` / `:core:utils` because it is a mid-level module and something similar happens
  with `:core:persistecnce` which uses `:core:preferences` / `:core:appearance`)  and nothing else; they are in turn
  used by all the other types of modules.

### Top-level modules

The main module (Android-specific) is `:androidApp`, which contains the Application subclass (`MainApplication`) and the
main activity (`MainActivity`). The latter in its `onCreate(Bundle?)` invokes the `MainView` Composable function which
in turns calls `App`, the main entry point of the multiplatform application which is defined in the `:shared` module.

`:shared` is the top module of the multiplatform application, which includes all the other modules and is not included
by anything (except `:androidApp`). In its `commonMain` source set, this module contains `App`, the application entry
point, the definition on the `MainScreen` (and its ViewModel) hosting the main navigation with the bottom tab bar.
Another important part of this module resides in the platform specific source sets (`androidMain` and `iosMain`
respectively) where two `DiHelper.kt` files (one for each platform) can be found, which contain the setup of the root of
the project's dependency injection in a platform specific way, an initialization function on iOS and a Koin module for
Android (which is included in `MainApplication`).

### Feature modules

These modules correspond to the main functions of the application, i.e. the sections of the main bottom navigation. In
particular:

- `:feature:home` contains the post list tab;
- `:feature:search` contains the Explore tab;
- `:feature:inbox` contains the Inbox tab;
- `:feature:profile` contains the Profile tab;
- `:feature:settings` contains the Settings tab.

### Domain modules

These are purely business logic modules that can be reused to provide application main parts:

- `:domain:identity` contains the repositories and use cases that are related to user identity, authorization and API
  configuration;
- `:domain:lemmy` contains all the Lemmy API interaction logic and is divided into two submodules:
    - `:data` contains all the domain models for Lemmy entities (posts, comments, communities, users, etc);
    - `:repository` contains the repositories that access Lemmy APIs (through the :core:api module) and are used
      to manage the entities contained in the :data module;
- `:domain:inbox` contains some uses cases needed to interact with the replies, mentions and private messages
  repositories and coordinate the interaction between inbox-related app components.

### Unit modules

These modules are the building blocks that are used to create user-visible parts of the application, i.e. the various
screens, some of which are reusable in multiple points (e.g. the user detail, community detail or post detail, but also
report/post/comment creation forms, etc.). In some cases even a dialog or a bottom-sheet can become a "unit", especially
if it is used in multiple points or contains a little more than pure UI (e.g. some presentation logic); simple pure-UI
dialogs and sheets are located in the `:core:commonui:modals` module instead (but are being progressively converted to
separate units).

Here is a list of the main unit modules and their purpose:

- `:unit:about` contains the About this app dialog
- `:unit:accountsettings` contains the screen of the remote account settings (web preferences)
- `:unit:ban` contains the modal bottom sheet used to ban a user from a community
- `:unit:chat` contains the chat conversation screen
- `:unit:choosecolor` contains the dialogs and bottom sheets used for theme/color selection
- `:unit:choosefont` contains the bottom sheets used for font selection
- `:unit:communitydetail` contains the community detail screen
- `:unit:communityinfo` contains the community info bottom sheet accessible from community detail
- `:unit:configureswipeactions` contains the screen and bottom sheets to configure swipe actions
- `:unit:configurecontentview` contains the screen used to configure post and comment appearance
- `:unit:createcomment` contains the create comment form
- `:unit:createpost` contains the create post form
- `:unit:drafts` contains the screen uses to display post and comment drafts
- `:unit:drawer` contains the navigation drawer
- `:unit:filteredcontents` contains the screen to access moderated contents or liked/disliked contents
- `:unit:instanceinfo` contains the instance info bottom sheet with the list of communities
- `:unit:login` contains the login modal bottom sheet
- `:unit:manageaccounts` contains the modal bottom sheet used to change account
- `:unit:manageban` contains the ban management screen
- `:unit:managesubscriptions` contains the subscription management screen
- `:unit:mentions` contains the mentions section of the Inbox tab
- `:unit:messages` contains the private messages section of the Inbox tab
- `:unit:modlog` contains the moderation log screen
- `:unit:multicommunity` contains the multi-community detail and multi-community editor screens
- `:unit:myaccount` contains the profile logged section of the Profile tab
- `:unit:postdetail` contains the post detail screen
- `:unit:postlist` contains the post list (home) screen
- `:unit:rawcontent` contains the "Raw content" dialog with the custom (Android) text toolbar
- `:unit:remove` contains the modal bottom sheet to remove a content (for moderators)
- `:unit:replies` contains the replies section of the Inbox tab
- `:unit:reportlist` contains the report list screen (for moderators)
- `:unit:saveditems` contains the saved items screen
- `:unit:selectcommunity` contains the dialog used to select communities (for cross-posts)
- `:unit:selectinstance` contains the bottom sheet used to change instance in anonymous mode
- `:unit:userdetail` contains the user detail screen
- `:unit:userinfo` contains the user information bottom sheet accessible from user detail
- `:unit:web` contains the internal WebView screen
- `:unit:zoomableimage` contains the image detail screen used to display full-screen images.

### Core modules

These are the foundational blocks containing the design system and various reusable utilities that are called throughout
the whole project. Here is a short description of them:

- `:core:api` contains the Ktorfit services used to interact with Lemmy APIs and all the data transfer objects (DTOs)
  used to send and receive data from the APIs;
- `:core:appearance` contains the look and feel repository which exposes the information about the current theme as
  observable states and allows to change them;
- `:core:architecture` contains the building blocks for the Model-View-Intent architecture used in all the screens of
  the application;
- `:core:commonui` contains a series of submodules that are used to define UI components used in the app and reusable
  UI blocks:
    - `:components`: a collection of components that represent graphical widgets
    - `:detailopener-api` : a utility module used to expose an API to centralize content opening (post detail,
      community, detail, user detail, comment creation and post creation)
    - `:detailopener-impl`: implementation of the detail opener, this is an exception to the module architecture because
      it is a core module which includes unit modules so the important thing is
      that no one **ever** include this module except for `:shared`;
    - `:lemmyui`: graphical components used to represent Lemmy UI (posts, comments, inbox items, etc.) and reusable
      subcomponents such as different types of headers, footers, cards, etc.
    - `:modals`: definition of modal bottom sheets and dialogs that have no presentation logic. This module was
      historically much bigger and over time components were migrated to separate units
      modules;
- `:core:markdown` contains Markdown rendering logic;
- `core:l10n` contains all the localization messages and the `L10nManager` interface which acts as a wrapper around
  Lyricist to load the internationalized messages;
- `:core:navigation` contains the navigation manager used for stack navigation, bottom sheet navigation and a
  coordinator for the events originated by the navigation drawer;
- `:core:notifications` contains the `NotificationCenter` contract and implementation as well as the event definition,
  this is used as an event bus throughout the whole project;
- `:core:persistence` contains the local database (primary storage) management logic as well as SQLDelight definitions
  of entities and migrations, plus all the local data sources that are used
  to access the database;
- `:core:preferences` contains the shared preferences/user defaults (secondary storage) and relies on the
  multiplatform-settings library to offer a temporary key-value store;
- `:core:resources` is a wrapper around the resource loading (fonts and images mainly) which used to rely on an external
  library and now used the built-in resource management of Compose;
- `:core:utils`: contains a series of helper and utility functions/classes that are used in the project but were not big
  enough to be converted to separate domain/core modules on their own.

On second thoughts:

- `:core:commonui` has still too much in it, especially `:modals` packages should become unit modules;
- `:core:persistence` belongs more to domain modules, e.g. `:domain:accounts`/`:domain:settings` but it is implemented
  as a core module because is is strongly tied to SQLDelight and its generated code which provides the named queries to
  fetch/save data to the local DB.
