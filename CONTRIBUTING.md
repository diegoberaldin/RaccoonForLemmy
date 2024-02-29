# CONTRIBUTING.md aka «the trash Panda's guide to the galaxy»

## 0. Introduction

### 0.1 Welcome

First of all, a word of welcoming to whomever has landed on this page. If you are here, that means
that somehow you have found a link to this project over the Internet, you are possibly interested in
it (either positively or negatively) and you are wondering whether you can contribute in any way.

This is great news, so thank you for your time and dedication. Whether you spend 1 minute or 1 hour
here, it is really highly appreciated.

### 0.2 ToC

With no further ado, here is a short list of what you'll find here. Keep in mind that, like the
project in under continuous development and evolution (hopefully), so is this guide. New sections
will be added and new answers will be provided as long as questions are submitted.

- [section 1](#1-project-overview) will provide a general overview about the project and its
  contributors: who we are, what we are trying to achieve, why are we doing it and how;
- [section 2](#2-community) illustrates the core values that are behind this project, its key goals
  and an acknowledgement to all the people who helped us to achieve those targets;
- [section 3](#3-how-tos) is a collection of operative instructions for those who want to
  contribute, either reporting bugs, asking for new features, submitting feedback, contributing with
  code, documentation or translations;
- [section 4](#4-setup-the-development-environment) is dedicated to developers wanting to build the
  project locally in order to submit pull requests;
- [section 5](#5-project-structure) contains a technical illustration of the tech stack used in the
  project how the project is broken down into discrete components that interact with each other;
- [section 6](#6-conventions) contains the architectural patterns used in the project and the
  coding conventions you should follow when submitting PRs, because readability and consistency
  matter and there are some rules (with exceptions) that should ideally be followed everywhere.

## 1 Project overview

### 1.1 Purpose

Raccoon for Lemmy was designed with three goals in mind: on the one side experiment and put to some
stress test with a real world project Kotlin Multiplatform (KMP) and Compose multiplatform, in order
to explore what could be achieved with multiplatform libraries and share as much code as possible
in the `commonMain` source directory. The Android ecosystem of Lemmy apps has many cross-platform
alternatives implemented with Flutter or React, a couple of native options written in Java and just
one native pure-Kotlin application. Raccoon intends to be an Android-first app, but
experimentation on other OSes given the multiplatform nature of its technologies are not
excluded _a priori_.

The second goal was to offer a feature rich Lemmy client mainly aimed at "pro" users, i.e. users who
are not content with just browsing the contents of the Fediverse, creating posts and
answers. This is why Raccoon tried to offer from the very beginning features like:

- a full-fledged explore section that allows multiple result types
- instance info, with the list of communities of foreign instances as well as the possibility to
  visit communities on foreign instances in guest mode
- multi-community, i.e. community aggregation
- moderation tools (for community moderators) and access to community moderation log.

Finally, the third goal was to offer a Lemmy experience where everyone could "feel at home" (see the
[Code of Conduct](#22-code-of-conduct) below). This implies keeping the interface lean by default
but having the possibility to customize a variety of aspects (theme, vote format, default feed/sort
type) and adopt the user's native language. As a consequence, leanness, personalization and
localization are first-class citizens in this project.

### 1.2 About the name

Lemmy users used to refer to themselves as "lemmings" and the platform logo clearly recalls that
animal, so there was this tradition in the early days to use animals names for clients -- or at
least the original developers of this app thought so (some examples being Lemmur, Jerboa and
Fennec). Since they really liked raccoons, they choose that: raccoons are cunning animals that adapt
to live in harsh conditions, finding a way to survive even with what we consider "trash". They look
like masked criminals but do not harm anyone, they are lazy, chubby and absolutely cute.

In this group, we often quote every raccoon's motto: «Live Fast, Eat Trash» (abbreviated L.F.E.T.).

## 2 Community

### 2.1 About us

This section is probably going to change over time, anyway we are a small group of volunteers who in
their spare time contribute to the project.

To ask more questions or chat directly with people, you can find us
on [this Discord server](https://discord.gg/erxQRFjJ).

### 2.2 Code of Conduct

When interacting with other members of the community, be them end-users, developers, supporters or
whatever, remember that we are raccoons, and we follow these principles:

|   | Principle         | Explanation                                                                                                                                       |
|:-:|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| R | **Respect**       | We respect each other as people, remembering we are part of a group that goes beyond individual opinions, beliefs, preferences and habits.        |
| A | **Availability**  | We support each other with the skills and available time that we have, considering that we are volunteers and we operate on a best-effort basis.  |
| C | **Commitment**    | We are responsible for our choices and we agree that our choices are taken wisely, always considering the impact on other members of the group.   |
| C | **Cohesion**      | We remember that we are part of a community which is bigger than the individual members, so we do unto others as we would have them do unto us.   |
| O | **Objectivity**   | We are committed to telling the truth in the most objective way and, if we express subjective opinions, to do it in a clear and constructive way. |
| O | **Originality**   | We bring our own personal experience and ideas which are acceptable even if "different": everyone's voice matters and deserves to be listened.    |
| N | **Never give up** | We are tenacious and are not afraid by technical challenges, we embrace every difficult task as an opportunity to learn and acquire new skills.   |

## 3. HOW-TOs

### 3.1 General interactions

There are many ways you can interact with members of the community:

- use the [Discord server](https://discord.gg/erxQRFjJ) to chat in real time with other team
  members, there are two rooms in the space: "General" is for general information about the app
  development and "Trashcan" is more for smalltalk and random topics;
- use the [Lemmy community](https://lemmy.world/c/raccoonforlemmy) to receive updates about the new
  releases, participate into public discussions in the Lemmy style and provide your feedback or even
  share your memes about raccoons with any other interested people;
- use the [GitHub issue tracker](https://github.com/diegoberaldin/RaccoonForLemmy/issues) to report
  bugs or request features;
- use the [GitHub discussion section](https://github.com/diegoberaldin/RaccoonForLemmy/discussions)
  for technical questions about the release roadmap, questions about app internationalization, etc.;
- finally, if none of the above methods fits your needs you can write an email or send a private
  message to the original developer (my personal information are in the GitHub profile).

Always remember the guidelines
contained [here](https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/CODE_OF_CONDUCT.md)
and [here](#22-code-of-conduct) to interact
with other people: our values are important and should be applied even with each other and not only
with end users.

### 3.2 Create bug reports

The preferred way to submit a bug report is
the [GitHub issue tracker](https://github.com/diegoberaldin/RaccoonForLemmy/issues).

- use the "New issue" button to open the creation form
- in the "Add a title" field give a synthetic description of the issue, if it affects a specific
  part of the application you can add in square brackets a prefix containing that information,
  e.g. `[post list]` or `post detail` or `[create report]`.
- in the "Add a description" field provide a more detailed explanation of the issue, you can use
  Markdown syntax (in the GitHub flavour) to format text here.
  An ideal bug report contains:
    - the precondition (sequence of steps that lead to the faulty condition, if it is
      deterministic)
    - a description of the error condition (with the expected result and the actual
      result, where the difference between the two is clear)
    - (if you can, optionally) some screenshots that make it clear where the error is
- you can add the "bug" label to the issue or otherwise leave it blank. Do not use the other
  fields (assignee, milestone, etc.) because they are used internally by the team members who will
  take the issue in charge during resolution.

### 3.3 Request features

As for bug reports, the preferred way for feature or change requests is
the [GitHub issue tracker](https://github.com/diegoberaldin/RaccoonForLemmy/issues).
In this case, you should:

- insert a short description in the title field, if the feature is specific to some app domain you
  can use the same square bracket prefix as in bug reports
- insert a more detailed explanation of your idea in the description field, if your feature is
  present in other apps you can provide screenshots or make references to make the request clearer
- you can use the "feature" label to tag the issue, or otherwise leave it blank and let the
  maintainers decide how to triage the issue.

### 3.4 Add a new localization

The preferred way for localizations (l10ns) is to submit a pull request (PR) as detailed in
the [next section](#35-submit-a-pull-request). The project uses
the [Lyricist](https://github.com/adrielcafe/lyricist) library with a KSP plug-in that parses
XML files in the Android style to generate values that can be accessed via composition locals.

You will have to create a new folder under the `core/l10n/src/androidMain/res` directory
named `values-XX` where XX is locale you want to add (following IANA conventions) and create
a `strings.xml` file in it, copying the contents
of [this file](https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/core/l10n/src/androidMain/res/values/strings.xml)
containing the base localization.

After which you'll have to make sure the project compiles, because there are some syntax rules that
must be enforced in string files, e.g.:

- apostrophes (`'`) must be escaped with a backslash (`\'`)
- some special characters must be represented as XML entities so `>` becomes `&gt;`, `<`
  becomes `&lt;`, `&` becomes `&amps;` and so on… (talking of ellipsis, use `…` preferably instead
  of three dots)

If you want you can change the code in the following spots:

- in
  [Extensions.kt](https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/core/utils/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/utils/Extensions.kt)
  add your flag and language name (mapping your language code to the values)
- add an option (using your language code) in the `values` array
  in [LanguageBottomSheet.kt](https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/core/commonui/modals/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/commonui/modals/LanguageBottomSheet.kt)

That's it. You can test that everything works by launching the development app.

**A note for translators:** the strings that correspond to theme names (`settings_color_aquamarine`,
`settings_color_banana`, `settings_color_blue`, `settings_color_gray`, `settings_color_green`,
`settings_color_orange`, `settings_color_pink`, `settings_color_purple`, `settings_color_red` and
`settings_color_white`) contain the name of an animal accompanied by an adjective. If would be nice
if the adjective and the noun would start in every language with the same sound in order to create
humorous pairs like in English ("hilarious hedgehog", "frolicsome frog", etc.).

However, if you are not a developer and do not feel confident with GitHub's PR mechanism, you can
just download
the [base l10n](https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/resources/src/commonMain/resources/MR/base/strings.xml)
to your local machine, edit the file and send an email to the maintainers with the attachment, we
will take care of the rest.

If you have proposals, want to submit l10n fixes/improvements to existing ones, you can
use [this discussion](https://github.com/diegoberaldin/RaccoonForLemmy/discussions/378) and post an
answer, we will reach out to you and solve the issue.

### 3.5 Submit a pull request

First of all, please fork the repository by using the "Create a new fork" button in the GitHub
console, which will create a copy of the repository in your personal account.

Create a branch from the repository's master branch named after the feature/change you want to
submit, use whatever convention you like (preferably underscores to separate words but as you wish)
and make any commits you want on it.

Push the commits on the remote branch of your forked repository.

Use the "Compare and pull request" button to see a recap of the PR, after which use the "Create pull
request" to submit the request to the upstream repository.

## 4. Setup the development environment

This project uses the Gradle build tool. The recommended development environment is Android Studio
with the Kotlin Multiplatform Mobile plugin installed, which will automatically detect the build
tool and download a Gradle distribution for you.

Since the project is using Gradle 8.5 with the Android Gradle Plugin (AGP) version 8.2.2, please
make sure that you are using Android Studio Hedgehog or later -- have a
look [here](https://developer.android.com/build/releases/gradle-plugin?hl=en#android_gradle_plugin_and_android_studio_compatibility)
for a compatibility matrix between versions of Gradle, AGP and Android Studio.

In order for Gradle to build, you will need a JDK installed on your local development machine, if
you are using stock Android Studio it ships with the JetBrains runtime. If you want to use your
custom JDK (e.g. under Linux you want to try OpenJDK instead), please make sure that it has a
suitable version, according
to [this page](https://docs.gradle.org/8.2/userguide/compatibility.html).

Finally, since building this project requires a lot of RAM, please make sure that
the `gradle.properties` file in the root folder contains proper memory settings for the JVM and the
Kotlin compile daemon:

```properties
org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8 -Dkotlin.daemon.jvm.options\="-Xmx4096M"
```

## 5. Project structure

The project is organized as a multi-module Gradle projects, meaning that it is split into a series
of interdependent Gradle subprojects and that there is a module hierarchy in which multiple levels.

The modules can be grouped into five groups:

- **top-level and special modules**: these modules are tied to the project setup and contain the
  entry point of the app (include everything);
- **feature modules**: these that correspond to the main functions of the application, i.e. the
  five tabs that can be found in the tab bar that live independently and made up the basic
  structure. Feature modules can be included by top-level modules but can not include each other.
  They can include domain modules, unit modules or core modules.
- **domain modules**: a series of purely business logic modules containing code related to specific
  domains such as user identity management, all the Lemmy specific business logic (APIs and inbox).
  Domain modules can only include core modules and can be included by feature modules, unit modules
  or top-level ones.
- **unit modules**: reusable parts of the application containing UI components and a thin layer of
  presentation logic that are used to render the various screens, bottom sheets and dialogs of the
  application, grouped by feature so that each block can be called from multiple points. Unit
  modules are used by feature modules and can include domain modules, core modules or in some
  limited cases other unit modules (but never cyclically!).
- **core modules** are the foundational layer of the application. They are included by unit modules,
  feature modules, domain modules and top-level modules. They should not include anything except in
  some rare occasions, other core modules (but never cyclically!). A notable example of this is the
  `:core:markdown` module (Markdown rendering) that includes `:core:commonui:components` because
  Markdown
  requires some custom UI components to be rendered.

For more detailed information about the contents of each group and the purpose of each module,
please refer
to [this page](https://diegoberaldin.github.io/RaccoonForLemmy/tech_manual/main#module-structure).

## 6. Conventions

### 6.1 App architecture

#### 6.1.1 General principles

The project’s architecture is inspired from the Clean architecture principles. Modules are organized
in tiers and dependency flows goes from higher modules (top-level and feature modules) towards lower
order modules (unit modules for UI, domain modules for business logic) which in turn rely on core
modules for low-level operations; and that is a hierarchical structure on the vertical axis. Apart
from that, there is a split on the horizontal axis (i.e. different features for different sections
of the app). The intersection between these two axes determines the modularization strategy.

To summarize, if you are looking for something related to the app UI, have a look at `:feature:xxx`
or `:unit:xxx` (and possibly you will have to drill down to `:core:commonui:xxx`). If you are
searching for some piece of interaction with Lemmy APIs, have a look at `:domain:lemmy:xxx` (and
you’ll probably end up in `:core:api` for more low-level minutia), if you look for data access on
the local database have a look at `:core:persistence`, and so on. More on this here (Modularization
strategy).

#### 6.1.2 Model-View-Intent

Every part of the app which has some non trivial user-interaction follows the Model-View-Intent
architectural pattern. This means that there are two different components interacting with each
other:

- the View, represented by a `Screen` implementation
- the ViewModel, represented by a `ScreenModel` implementation

where the interfaces (Screen and ScreenModel) both come from the Voyager navigation library.

The **View** has the responsibility of drawing to screen the UI components that are needed to
represent a particular **state**, which is provided by the ViewModel they have a reference to (the
ViewModel being a state holder for this respect). The View has also the ability to collect user
input (or, rather, intentions as **intents**) and dispatch them to the ViewModel.

On its side, the **ViewModel** holds an observable state and has the ability to react to user
intents coming from the View, each of which implies some business logic operations which, in turn,
determine ultimately a state change, observed by the view. In some less frequent case, the ViewModel
can emit one-time events, that are unrelated to persistent state and can determine volatile
**effects** in the View.

These concepts of a ViewModel having to deal with Intent, State and Effect are modelled in Kotlin in
the `:core:architecture` module which is included as a building block by all UI related features and
unit modules of the project. The `MviModel` interface defined here is a common supertype of all the
ScreenModel implementations.

In case some event needs to be propagated across different ViewModels, the event bus defined
in `:core:notifications` is used.

## 6.2 Coding rules

### 6.2.1 General Kotlin rules

Please refer to [this page](https://kotlinlang.org/docs/coding-conventions.html) for the conventions
to apply to Kotlin code, most of which are already enforced by Android Studio inspections.

### 6.2.2 Compose rules

As far as Compose code is concerned, we take Google’s indications as a baseline:

- [general Compose API](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md)
- [Compose component API](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md)

### 6.3 Test structure

#### 6.3.1 Unit tests

Unit test are targeted as a single unit of code: the test class will have the same name of the
component under test, followed by the `Test` suffix and will be placed in the same package, within
the `androidUnitTest` source set. The tests will be platform specific for now, since multi-platform
tests under the `commonTest` set require some additional setup and a considerable amount of extra
effort.

For every subject under test (SUT), the dependencies will be doubled using test mocks created using
the `mockk` library, and the assertions on flows and channels will be made using the `turbine`
library.

Each test class will contain at least one method annotated with `@Test` and, especially if it
contains suspending functions, its body will be wrapped in a `runTest` scope function (better to
always include it). In order for suspending functions to be called in the correct coroutine
context (where the main thread of Android is replaced by an `Unconfined` dispatcher), the JUnit rule
`DispatcherTestRule` defined in :core:testutils should be included and annotated with `@get:Rule`.

Each test method shall consider a single interaction (W) on the SUT that happens in a
precondition (G) and should produce a result (T) against which some assertions will be performed.

These three elements will be reflected in the method name, which shall have the GWT form, i.e.:
```givenX_whenY_thenZ```

The body of the method will be therefore divided into three parts (separated by a blank line):

- (optional) precondition: mock setup with predefined answers to invocations;
- interaction: the method/property to test will be invoked/interacted with on the SUT;
- result: one or more assertions on the result got in the previous step; optionally this part will
  also contains some verification on the mocks/spies to make sure the proper interactions have or
  have not happened according to the expectations in the previous step.

Reference unit test:

```kotlin
class DefaultNavigationCoordinatorTest {

    // coroutine test rule (must be a public property)
    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    // subject under test
    private val sut = DefaultNavigationCoordinator()

    // test method (1)
    @Test
    fun givenNavigatorCanPop_whenRootNavigatorSet_thenCanPopIsUpdated() = runTest {
        // setup
        val navigator = mockk<Navigator> {
            every { canPop } returns true
        }

        // interaction
        sut.setRootNavigator(navigator)

        // assertions
        val value = sut.canPop.value
        assertTrue(value)
    }

    // test method (2)
    @Test
    fun whenChangeTab_thenCurrentTabIsUpdated() = runTest {
        // setup with capturing slots
        val tabSlot = slot<Tab>()
        val navigator = mockk<TabNavigator>(relaxUnitFun = true) {
            every { current = capture(tabSlot) } answers {}
        }
        val tab = object : Tab {
            override val options @Composable get() = TabOptions(index = 0u, "title")

            @Composable
            override fun Content() {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        sut.setTabNavigator(navigator)

        // interaction
        sut.changeTab(tab)

        // assertions
        val value = tabSlot.captured
        assertEquals(tab, value)
    }

    // test method (3)
    @Test
    fun whenSubmitDeeplink_thenValueIsEmitted() = runTest {
        val url = "deeplink-url"
        // interaction
        sut.submitDeeplink(url)

        // assertions on the flow with turbine's test extensions
        sut.deepLinkUrl.test {
            val value = awaitItem()
            assertEquals(url, value)
        }
    }
}
```