# Test scenarios

## Repositories Tests

### SUT for `UserRepositoryImpl` - `UserRepositoryImplTest`

1. When User Login Success
    - Make sure that result is `Resource.Success`
    - Make sure that actual result is equal with expected result
    - Make sure that the `storyApi.login()` method has been called with given parameters.
    - Make sure has no interaction with `dataStore`.

2. When User Login Failed
    - Make sure that result is `Resource.Error`
    - Make sure that error message is equal with expected result
    - Make sure that the `storyApi.login()` method has been called with given parameters.
    - Make sure has no interaction with `dataStore`.

3. When User Login Failed with `Exception`
    - Make sure that result is `Resource.Error`
    - Make sure that error message is equal with expected exception message
    - Make sure that the `storyApi.login()` method has been called with given parameters.
    - Make sure has no interaction with `dataStore`.

4. When User Register Success
    - Make sure that result is `Resource.Success`
    - Make sure that actual result is equal with expected result
    - Make sure that the `storyApi.register()` method has been called with given parameters.
    - Make sure has no interaction with `dataStore`.

5. When User Register Failed
    - Make sure that result is `Resource.Error`
    - Make sure that error message is equal with expected result
    - Make sure that the `storyApi.register()` method has been called with given parameters.
    - Make sure has no interaction with `dataStore`.

6. When User Register Failed with `Exception`
    - Make sure that result is `Resource.Error`
    - Make sure that error message is equal with expected exception message
    - Make sure that the `storyApi.register()` method has been called with given parameters.
    - Make sure has no interaction with `dataStore`.

7. When Current User is Available
    - Make sure that `user.token` is not null or empty,
    - Make sure that user result is equal with expected user,
    - Make sure that the `dataStore.data` has been called,
    - Make sure has no interaction with `storyApi`.

8. When Current User is Available
    - Make sure that `user.token` is null or empty,
    - Make sure that user result is equal with expected user,
    - Make sure that the `dataStore.data` has been called,
    - Make sure has no interaction with `storyApi`.

### SUT for `StoryRepositoryImpl` - `StoryRepositoryImplTest`

1. When Create Story Success
    - Make sure that result is `Resource.Success`
    - Make sure that expected result is equal with actual result
    - Make sure that the `storyApi.uploadStory()` method has been called with given parameters.
    - Make sure that Mappings input method has been called.
    - Make sure has no interaction with `database`.

2. When Create Story Failed
    - Make sure that result is `Resource.Error`
    - Make sure that expected error is equal with actual error
    - Make sure that the `storyApi.uploadStory()` method has been called with given parameters.
    - Make sure that Mappings input method has been called.
    - Make sure has no interaction with `database`.

3. When Create Story Failed with `Exception`
    - Make sure that result is `Resource.Error`
    - Make sure that expected exception is equal with actual exception
    - Make sure that the `storyApi.uploadStory()` method has been called with given parameters.
    - Make sure that Mappings input method has been called.
    - Make sure has no interaction with `database`.

4. When Get Stories Success
    - Make sure that result is `Resource.Success`
    - Make sure that expected result is equal with actual result
    - Make sure that the `storyApi.getStories()` method has been called with given parameters.

5. When Get Stories Failed with `Exception`
    - Make sure that result is `Resource.Error`
    - Make sure that expected exception is equal with actual exception
    - Make sure that the `storyApi.getStories()` method has been called with given parameters.

## View Models Tests

### SUT for `CreateStoryViewModel` - `CreateStoryViewModelTest`

1. When Create Story with empty input (Internal Validation)
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Error`
    - Make sure that the expected result is equal with actual exception
    - Make sure has no interaction with `storyRepository`.

2. When Create Story with invalid input (Server Validation)
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Error`
    - Make sure that expected error is equal with actual error
    - Make sure that the `storyRepository.createStory()` method has been called with given input.

3. When Create Story with valid input
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Success`
    - Make sure that expected message is equal with actual message
    - Make sure that the `storyRepository.createStory()` method has been called with given input.

### SUT for `HomeViewModel` - `HomeViewModelTest`

1. When Collect Stories Success
    - Make sure that actual result is Not Empty
    - Make sure that size of  `expectedResult` is equal with `actualResult`
    - Make sure that `expectedResult` data is equal with `actualResult`
    - Make sure that the `storyRepository.getPagedStories()` method has been called.

2. When Collect Stories Failed (empty data)
    - Make sure that actual result is Empty
    - Make sure that the `storyRepository.getPagedStories()` method has been called.

### SUT for `StoriesMapViewModel` - `StoriesMapViewModelTest`

1. When Map is Not Ready
    - Make sure has no interaction with `storyRepository`.

2. When Map Ready **(Load Stories Success)**
    - Make sure that result is `UiState.Success`
    - Make sure that expected result is equal with actual result
    - Make sure that the `storyRepository.getStories()` method has been called.

3. When Map Ready **(Load Stories Failed)**
    - Make sure that result is `UiState.Success`
    - Make sure that expected error is equal with actual exception
    - Make sure that the `storyRepository.getStories()` method has been called.

5. When `numLoadedStories` Change than Reload Stories
    - Make sure that `numLoadedStories` is equal with change `numLoadedStories`
    - Make sure that stories result is `UiState.Success`
    - Make sure that size of stories result is less than or equal with  `numLoadedStories`
    - Make sure that expected stories is equal with actual stories
    - Make sure that the `storyRepository.getPagedStories()` method has been called with valid parameters.

### SUT for `LoginViewModel` - `LoginViewModelTest`

1. When Login with empty input (Internal Validation) then Error
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Error`
    - Make sure that the expected result is equal with actual exception
    - Make sure has no interaction with `storyRepository`.

2. When Login  with valid input (Server Validation) then Error
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Error`
    - Make sure that expected error is equal with actual error
    - Make sure that the `storyRepository.signIn()` method has been called with given input.

3. When Login with valid input and then Success
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Success`
    - Make sure that expected user is equal with actual user
    - Make sure that the `storyRepository.signIn()` method has been called with given input.
    - Make sure that the `storyRepository.saveUserSession()` method has been called.

### SUT for `RegisterViewModel` - `RegisterViewModelTest`

1. When Register with empty input (Internal Validation) then Error
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Error`
    - Make sure that the expected result is equal with actual exception
    - Make sure has no interaction with `storyRepository`.

2. When Register with valid input (Server Validation) then Error
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Error`
    - Make sure that expected error is equal with actual error
    - Make sure that the `storyRepository.signUp()` method has been called with given input.

3. When Register with valid input and then Success
    - Make sure that `formState` is `FormState.Submit`
    - Make sure that `submitState` is `UiState.Success`
    - Make sure that expected user is equal with actual user
    - Make sure that the `storyRepository.signUp()` method has been called with given input.

### SUT for `MainViewModel`  - `MainViewModelTest`

1. When User is Login
    - Make sure that `isLogin` is `true`
    - Make sure that the `userRepository.getCurrentUser()` method has been called.

2. When User **Not** Login
    - Make sure that `isLogin` is `false`
    - Make sure that the `userRepository.getCurrentUser()` method has been called.

3. When User `logOut`
    - Make sure that the `userRepository.signOut()` method has been called.


## UI Tests

### SUT for `LoginFragment` - `LoginFragmentTest` **@SmallTest**

1. **@MediumTest** - Login with valid input  then `login button` enable
    - Make sure initial `login button` is disable
    - type valid email on Edit Text `email`
    - type valid password on Edit Text `password`
    - Make sure initial `login button` is enable

2. **@MediumTest** -  type invalid  input
    - Make sure initial `login button` is disable
    - type invalid email on Edit Text `email` then make sure error message is appear
    - type invalid password on Edit Text `password` then make sure error message is appear
    - Make sure `login button` still disable

### SUT for `RegisterFragment` - `RegisterFragmentTest` **@SmallTest**

1. Register with valid input  then `login button` enable
    - Make sure initial `register button` is disable
    - type valid name on Edit Text `name`
    - type valid email on Edit Text `email`
    - type valid password on Edit Text `password`
    - Make sure initial`register button` is enable

2. Register with invalid  input then `login button` disable
    - Make sure initial `register button` is disable
    - type invalid email on Edit Text `email` then make sure error message is appear
    - type invalid password on Edit Text `password` then make sure error message is appear
    - Make sure `register button` still disable

### SUT for `DetailStoryFragment` - `DetailStoryFragmentTest` **@SmallTest**

1. Check view and make sure data display equal with given bundle
    - Make sure detail name is shown and text is valid
    - Make sure create at is shown and text is valid
    - Make sure description is shown and text is valid
    - Make sure Image is shown, adn have content description

### SUT for `HomeFragment` - `HomeFragmentTest` **@MediumTest**

1. Launch Fragment load data Error *(doesn't have cache)*
   - Make sure `recylerView` is hide
   - make sure `retryButton` is shown
   - make sure `errorMsg` is shown

2. Launch Fragment load data Error *(have cache)*
   - Make sure `recylerView` is shown and have data
   - make sure `recylerView` at position 0 is shown and error layout is append on current position
   - make sure `retryButton` is hide
   - make sure `errorMsg` is hide

3. Launch Fragment load data Success
   - Make sure `recylerView` is shown and have data
   - make sure `retryButton` is hide
   - make sure `errorMsg` is hide

### SUT for `MainActivity` - `MainActivityTest` (**@LargeTest**)
1. End-to-End: Authentication Flow
   this test is when user open app than doesn't have session (unAuthenticated)
   - Make sure current destination is `loginFragment`
   - type valid input email and password
   - Make sure all input doesn't have error and `loginbutton` is enabled.
   - click on `loginbutton` and wait until success
   - Make sure current destination is `homeFragment`
   - Make sure `homeList` is displayed.
   - perform `logout` and make sure current destination is `loginFragment`

2. End-to-End: Application Flow
   this test is when user open app than have session **(Authenticated)**
   - Make sure current destination is `homeFragment`
   - Make sure `homeList` is displayed, and perform click on list
   - Make sure current destination is `detailFragment` and layout is corrent
   - press back and maksure current destination is `homeFragment`
   - perform click on `floatingActionButton` and make sure current destination is `createFragment`

## Database and Remote Mediator Tests

### SUT for `StoryRemoteMediator` - `StoryRemoteMediatorTest`
1. Refresh Load then Error IOException
   - Make sure result is `MediatorResult.Error`
   - Make sure error throwable is IOException

2. Refresh Load then Success (EndOFPagination)
   - Make sure result is `MediatorResult.Success`
   - Make sure `result.endOfPaginationReached` is true

3. Refresh Load then Success (More Data is Present)
   - Make sure result is `MediatorResult.Success`
   - Make sure `result.endOfPaginationReached` is false

### SUT for `StoryDb` - `StoryDbTest`
1. Insert Story and key Success
   - Make sure size of Result is Equal with given data size
   - Make sure size of inserted story is equal with size of inserted remoteKey
