# FatFight - Android App

**FatFight** is an Android application developed as a solution for an exam in Android development. The app is designed to help users manage their diet and make healthy meal choices by offering customizable recipe suggestions for different meals. While the app showcases various Android development skills—such as API integration, RecyclerView management, and local data storage—it is not for real-life use and lacks features that would enhance user experience in a production environment.

![android](https://github.com/user-attachments/assets/16180397-16d1-4c87-82c3-fbfbefbfdfbb)


## Features

- **Home Screen**: Displays suggested recipes based on time of day or user preferences, with interactive options to like, add, or subtract calories for each meal.
- **Search Functionality**: Search for recipes using a search bar; recent searches are displayed for quick access.
- **Settings Screen**: Customize calorie intake and meal preferences through spinners and input fields.
- **Search History**: A RecyclerView showing recent searches that allows users to quickly search again.
- **Responsive Design**: Optimized for various screen sizes with a dynamic layout.

## Technology Stack

- **Android Components**: Single `MainActivity` with fragments (`FragmentMain`, `FragmentSettings`) for different functionalities.
- **RecyclerView Adapters**: Custom adapters for displaying search history and main recipe lists with dynamic content updates.
- **API Integration**: Uses `downloadAssetList()` to fetch recipe data from an external API.
- **Database**: SQLite database with tables for `search_history` and `liked_recipes`.
- **SharedPreferences**: Used for storing user settings and app state.

## How It Works

1. **Initialization**: `MainActivity` sets up `FragmentMain` on launch and initializes shared preferences and views.
2. **Search and Display**: The `doSearch()` function performs searches, updates views, and manages search history.
3. **Data Fetching**: `downloadAssetList()` retrieves data asynchronously, while `downloadImages()` fetches images in the background for a responsive UI.
4. **Settings Management**: `FragmentSettings` allows users to adjust their preferences, stored using `SharedPreferences`.
5. **Adapters**: `SearchHistoryAdapter` and `MainPageAdapter` manage dynamic content in RecyclerViews for smooth interaction.

## Future Improvements

- Add a fragment for viewing liked recipes.
- Implement infinite scrolling to load more recipes when reaching the bottom of the home screen.


## Authors

Developed by August and Pascal.
