## 1 Anonymous user

### 1.1 Home screen

The home screen is made up by three components:

- the top bar where you can see the feed type ("All" or "Local"), the instance name "via ..." and
  the sort type;
- the post list, where for each post you can see the community and creator info, its title,
  image (if any), URL (if any), number of comments, most recent modification date and the vote;
- the bottom navigation bar, from which you can navigate to other application sections.

The format of each post item (card, compact or full) and the vote format (aggregated, separated,
percentage) as well as the font size/face can be configured in the Settings screen.

If you open the menu of the floating action button in the bottom right corner, you can go back to
the top or activate the "zombie mode" (i.e. automatic scrolling).

![home screen](images/anon_post_list.png)
![floating action button menu](images/anon_home_fab.png)

### 1.2 Post detail

By selecting a post you can open the corresponding detail screen.

The detail screen shows the post creator and community info, title, cover image, URL, score and date
but also the textual body of the post if it is present.

After the post you can see the comments, each of which displays the creator, date, number of replies
and vote.

By using the "…" button you can access an additional menu about the post, e.g. you can see the raw
Markdown and inspect/copy its contents.

![post detail](images/anon_post_detail.png)
![see raw post](images/anon_see_raw.png)

### 1.3 Side menu

In anonymous mode, the side menu gives you the opportunity to change instance (the same menu can be
accessed by tapping the "via ..." label in the home top bar).

If your preferred instance is not listed, you can add a new one using the "+" button.

![side menu select instance](images/anon_select_instance.png)
![add instance](images/anon_add_instance.png)

### 1.4 Community detail

By tapping on the community info above each post title, you can open the community detail screen,
which is very similar to the home (you can activate the zombie mode here too).

If you tap on the "⋮" button in the top right corner you can also access some additional
information.

![community detail](images/anon_community_detail.png)
![user info](images/anon_community_info.png)

### 1.5 User detail

Conversely, if you tap on the user name above the title, you will access the user detail screen
where it is possible to see the posts and comments created by that particular user.

If you tap on the "⋮" button in the top right corner you can also access some additional
information.

![user detail](images/anon_user_detail.png)
![user info](images/anon_user_info.png)

### 1.6 Explore

The explore screens allows you to search Lemmy (among All or local feeds) for contents and filter by
a query string and result type (post, comment, communities, users or everything).

![explore](images/anon_explore.png)

### 1.7 Profile and Inbox

In anonymous mode the profile and inbox screens are just placeholders that invite you to access with
an account.

![anonymous profile](images/anon_profile.png)
![anonymous inbox](images/anon_inbox.png)

### 1.8 Settings

The settings screen allows you to customize the look and behaviour of your app.

- Look and feel:
    - Language: choose UI language (app specific);
    - UI theme: choose between light, dark, totally dark or system theme;
    - System notification and navigation bar theme: if you enable edge-to-edge display, choosing "
      Transparent" makes the status and nav bar completely transparent, otherwise they have the
      system color with a slight alpha to make them more visible;
    - Show navigation bar titles: include the section titles in the bottom navigation bar;
    - Colors and fonts: opens the color and font configuration screen;
- Post and comments:
    - Post layout: choose a layout (Card, Compact, Full) for posts;
    - Use display names for users and communities: if enabled, uses the user display name and the
      community title whenever possible, otherwise fallbacks to Lemmy handles (
      e.g. `!raccoonforlemmy@lemmy.world`);
    - Vote format: see the score of posts and comments as aggregated (`upvotes - downvotes`), split
      or percentage (`upvotes / (upvotes + downvotes) * 100`);
    - Full height images: show images with scaled width and full height (instead of capping the
      height) in the feed;
    - Default feed type: listing type for the home screen applied by default
    - Default post sort type: sort type for the home, community detail and user detail applied by
      default;
    - Default comment sort type: sort type for post detail applied by default to comments;
- Behaviour:
    - Edge to edge contents: enable the view from top to bottom edge for home, community detail,
      user detail, post detail and profile to maximize the space dedicated to contents;
    - Disable infinite scrolling: if this option is enabled, instead of automatically fetching new
      contents while scrolling (in home, community detail, post detail, user detail) an explicit "
      Load more" button is shown, in order to prevent the "doom scrolling" effect;
    - Zombie mode interval duration: amount of time between automatic scroll in "lazy mode";
    - Zombie mode scroll amount: translation amount for the automatic scroll in "lazy mode";
    - Hide navigation bar while scrolling: makes the bottom navigation bar invisible while scrolling
      down to maximize the space dedicated to contents;
    - Open links in external browser: rely on the external browser instead of in-app web view;
    - Automatically expand comments: expand all comment threads while opening the post detail;
    - Automatically load images: load images and videos automatically in post and comments;
    - Search posts only in title: enable exact match in the Explore section while searching posts;
- NSFW:
    - Include NSFW contents: determine whether NSFW contents are included in the feed by default;
    - Blur NSFW images: in home feed and community (unless the community is marked as NSFW as a
      whole) and user detail when a post is marked as NSFW, and the URL contains an image, the image
      is blurred;
- Debug:
    - Enable crash reporting: send anonymous reports about crash events;
    - About this app: shows a dialog with the app versions and some useful shortcuts to reach out to
      the developers or other members of the community;
    - User manual: opens the application manual online (this page).

![settings first part](images/anon_settings_1.jpg)
![settings second part](images/anon_settings_2.jpg)

#### 1.8.1 Colors and fonts

This screen allows to configure colors and fonts of the UI:

- Use dynamic colors: (on Android 12+) generate a palette based on your background dominant
  color;
- Custom theme color: (requires "Use dynamic color" option to be off) generate a palette by
  choosing a seed color from a predefined list or custom color;
- Comment bar theme: palette of colors used to distinguish nested comments;
- Comment bar width: allows to customize a multiplier applied to the width of comment bar indicating
  the depth on the left of each comment;
- UI font family: typeface used for the app UI elements (menus, labels, etc);
- Content font family: typeface used for post and comments on Lemmy;
- UI text size: scale factor to apply to the app UI elements;
- Content text size: scale factor to apply to posts and comments on Lemmy;

By the way, if you like Android dynamic colors and you think that on Lemmy there are plenty of
beautiful images you could set as your home screen background, have a look
at [this side project](https://github.com/diegoberaldin/CommonGround) of mine. It is a simple app
that allows you to configure remote image sources from the Lemmy-verse, preview how they look like
and set them easily as wallpapers for your home and lock screens.

![colors and fonts](images/anon_color_fonts.jpg)

### 1.9 Moderation log

The moderation log can be accessed from the community detail using the "⋮" button. It contains a
list of all the actions that happened for that given community, e.g.:

- users being promoted (or removed) to moderator;
- posts and comments being removed/restored;
- posts being locked (prevent further comments) or unlocked;
- posts being featured;
- comments being marked as distinguished;
- community being transferred.

![moderation log](images/logged_modlog.jpg)
