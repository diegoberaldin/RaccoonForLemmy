## Tech stack

Here is a list of the technologies used in the project, with a short historical explanation in the
cases where the initial choice changed over time and the reasons why the change was made.

<dl>
<dt>Dependency Injection</dt>
<dd>
The choice here is the <a href="https://github.com/InsertKoinIO/koin">Koin</a> library. The main 
reason it was selected because of its great multiplatform support and the integration with the 
navigation library (which at the beginning of the project was not there yet, but was added later and
proved to work great). You can find module definitions (beware, Gradle modules and Koin modules are
two different concepts and should not be confused) in a `di` package inside each subproject, modules
can include each other and all top-level modules are included in the shared module, more on it in
"Module overview and dependencies".
</dd>
<dt>Navigation</dt>
<dd>
For navigation the <a href="https://github.com/adrielcafe/voyager">Voyager</a> library has been
selected. Again, the choice was driven by its multi-platform support, its very practical approach 
and ease to set up and get going, compared to other multi-platform libraries like Decompose that
were used in the past. Nonetheless, and this lesson was learned the hard way, easiness of use and
compactness mean that things will go smooth in the future, and as the project grew the navigation
library started to show its limits. Part of them were addressed by encapsulating the transition 
logic (to push/pop screens into the navigation stack and open/close modal bottom sheets) into a 
centralized component <a href="https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/core/navigation/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/navigation/NavigationCoordinator.kt">NavigationCoordinator.kt</a>.
Something similar was done for the navigation drawer in <a href="https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/core/navigation/src/commonMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/navigation/DrawerCoordinator.kt">DrawerCoordinator.kt</a>.
Even the DI integration with Koin was not pain-free, the <code>:core:navigation</code> module contains
some glue code that is used to work around some of the issues that were encountered.
</dd>
<dt>Networking</dt>
<dd>Here, at least for Android developers, no surprises: the choice was <a href="https://github.com/ktorio/ktor">Ktor</a>
which has great multiplatform support. Instead of using Retrofit, to create network adapters the
<a href="https://github.com/Foso/Ktorfit">Ktorfit</a> library is used, which uses KSP to parse 
annotations and generate code.
</dd>
<dt>Resource management</dt>
<dd>
Initially the project used the <a href="https://github.com/icerockdev/moko-resources">Moko resources</a>
library to load fonts, icons and all the localized messages used in the UI. It worked great, since in those areas
Compose multiplatform missed the needed functionalities. Since resource management was introduced in Compose multiplatform
1.6.0, I migrated the project to the built-in system. And, for localization, the project was migrated to the
<a href="https://github.com/adrielcafe/lyricist">Lyricist</a> library which better handles dynamic language changes.
</dd>
<dt>Image loading</dt>
<dd>
This was something that was expected to be simpler but unfortunately it wasn't. Popular kotlin libraries
do not support multiplatform yet and the only library that advertised it was <a href="https://github.com/Kamel-Media/Kamel">Kamel</a>
which had a major bug while rendering large images, which took a long time to be considered (and possibly
has not been fixed yet). The project was already relying on Kamel for many things, from loading images on demand
to Markdown rendering, so deciding to switch was not easy at all. In the end, the iOS part of the project 
continues using it (Raccoon for iOS has by far bigger problems than image rendering, being a virtually
inexistent platform) while the Android counterpart was migrated to <a href="https://github.com/coil-kt/coil">Coil</a>.
Things will change when Coil 3.x will be release, because it will go multiplatform as well.
</dd>
<dt>Preference storage</dt>
<dd>
Here the choice was the <a href="https://github.com/russhwolf/multiplatform-settings">Multiplatform settings</a>
libary which not only works great but also offers support for encryption.
</dd>
<dt>Primary persistence</dt>
<dd>
This project was a chance to experiment with <a href="https://github.com/cashapp/sqldelight">SQLDelight</a>
(in other multiplatform projects other libraries were tested like Exposed), whereas database encryption
is obtained through <a href="https://www.zetetic.net/sqlcipher/sqlcipher-for-android">SQLCipher Android</a>, 
formerly <a href="https://github.com/sqlcipher/android-database-sqlcipher">Android Database SQLCipher</a>.
</dd>
<dt>Markdown rendering</dt>
<dd>
This was another part, like image loading, where KMP is still lacking and things are far more
complicated than it should be. The first approach that was used in the project, and which still survives
in the iOS platform (being it "no man's land" currently) involved using JetBrain's <a href="https://github.com/JetBrains/markdown">Markdown</a> 
library for parsing in conjunction with custom Compose rendering inspired by 
<a href="https://github.com/mikepenz/multiplatform-markdown-renderer">Multiplatform Markdown Renderer</a>.
This approach was promising in the beginning but it has proven to grow more and more difficult to 
support custom Markdown features, such as Lemmy spoilers. For this reason, the Android counterpart
has been completely refactored and migrated to the <a href="https://github.com/noties/Markwon">Markwon</a>
library which is more flexible/extensible albeit more complicated to use, especially if called from 
a multiplatform environment with <code>expect</code>/<code>actual</code> functions (and image opening/URL opening/custom links
like Lemmy URL references have to be managed). The big star here is <code>MarkwonProvider</code> and its implementation
<a href="https://github.com/diegoberaldin/RaccoonForLemmy/blob/master/core/md/src/androidMain/kotlin/com/github/diegoberaldin/raccoonforlemmy/core/markdown/provider/DefaultMarkwonProvider.kt">DefaultMarkwonProvider.kt</a>.
Parts of the Markwon configuration and usage is inspired by <a href="https://github.com/dessalines/jerboa">Jerboa for Lemmy</a>.
</dd>
<dt>Video playback</dt>
<dd>
This had to be native, the Android implementation relies on <code>Exoplayer</code> whereas the iOS implementation
on <code>AVPlayer</code> as usual.
</dd>
<dt>Theme generation</dt>
<dd>
The application allows to select a custom color and generate a Material 3 color scheme as a
palette originate from that seed color. This is achieved by using the <a href="https://github.com/jordond/MaterialKolor">MaterialKolor</a>
library which was designed to be multiplatform and works as a charm for the purpose. Thanks!
</dd>
</dl>
