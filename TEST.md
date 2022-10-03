
# Test scenarios

## Repositories

### SUT for `UserRepositoryImpl` - `UserRepositoryImplTest`

1. When User Login Success
    - Ensure that result is `Resource.Success`
    - Ensure that actual result is equal with expected result
    - Ensure that the `storyApi.login()` method has been called with given parameters.
    - Ensure has no interaction with `dataStore`.

2. When User Login Failed
    - Ensure that result is `Resource.Error`
    - Ensure that error message is equal with expected result
    - Ensure that the `storyApi.login()` method has been called with given parameters.
    - Ensure has no interaction with `dataStore`.

3. When User Login Failed with `Exception`
    - Ensure that result is `Resource.Error`
    - Ensure that error message is equal with expected exception message
    - Ensure that the `storyApi.login()` method has been called with given parameters.
    - Ensure has no interaction with `dataStore`.

4. When User Register Success
    - Ensure that result is `Resource.Success`
    - Ensure that actual result is equal with expected result
    - Ensure that the `storyApi.register()` method has been called with given parameters.
    - Ensure has no interaction with `dataStore`.

5. When User Register Failed
    - Ensure that result is `Resource.Error`
    - Ensure that error message is equal with expected result
    - Ensure that the `storyApi.register()` method has been called with given parameters.
    - Ensure has no interaction with `dataStore`.

6. When User Register Failed with `Exception`
    - Ensure that result is `Resource.Error`
    - Ensure that error message is equal with expected exception message
    - Ensure that the `storyApi.register()` method has been called with given parameters.
    - Ensure has no interaction with `dataStore`.

7. When Current User is Available
    - Ensure that `user.token` is not null or empty,
    - Ensure that user result is equal with expected user,
    - Ensure that the `dataStore.data` has been called,
    - Ensure has no interaction with `storyApi`.

8. When Current User is Available
    - Ensure that `user.token` is null or empty,
    - Ensure that user result is equal with expected user,
    - Ensure that the `dataStore.data` has been called,
    - Ensure has no interaction with `storyApi`.

### SUT for `StoryRepositoryImpl` - `StoryRepositoryImplTest`

1. When Create Story Success
    - Ensure that result is `Resource.Success`
    - Ensure that expected result is equal with actual result
    - Ensure that the `storyApi.uploadStory()` method has been called with given parameters.
    - Ensure that Mappings input method has been called.
    - Ensure has no interaction with `database`.

2. When Create Story Failed
    - Ensure that result is `Resource.Error`
    - Ensure that expected error is equal with actual error
    - Ensure that the `storyApi.uploadStory()` method has been called with given parameters.
    - Ensure that Mappings input method has been called.
    - Ensure has no interaction with `database`.

3. When Create Story Failed with `Exception`
    - Ensure that result is `Resource.Error`
    - Ensure that expected exception is equal with actual exception
    - Ensure that the `storyApi.uploadStory()` method has been called with given parameters.
    - Ensure that Mappings input method has been called.
    - Ensure has no interaction with `database`.

4. When Get Stories Success
    - Ensure that result is `Resource.Success`
    - Ensure that expected result is equal with actual result
    - Ensure that the `storyApi.getStories()` method has been called with given parameters.

5. When Get Stories Failed with `Exception`
    - Ensure that result is `Resource.Error`
    - Ensure that expected exception is equal with actual exception
    - Ensure that the `storyApi.getStories()` method has been called with given parameters.

## View Models

### SUT for `CreateStoryViewModel` - `CreateStoryViewModelTest`

1. When Create Story with empty input (Internal Validation)
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Error`
    - Ensure that the expected result is equal with actual exception
    - Ensure has no interaction with `storyRepository`.

2. When Create Story with invalid input (Server Validation)
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Error`
    - Ensure that expected error is equal with actual error
    - Ensure that the `storyRepository.createStory()` method has been called with given input.

3. When Create Story with valid input
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Success`
    - Ensure that expected message is equal with actual message
    - Ensure that the `storyRepository.createStory()` method has been called with given input.

### SUT for `HomeViewModel` - `HomeViewModelTest`

1. When Collect Stories Success
    - Ensure that actual result is Not Empty
    - Ensure that size of  `expectedResult` is equal with `actualResult`
    - Ensure that `expectedResult` data is equal with `actualResult`
    - Ensure that the `storyRepository.getPagedStories()` method has been called.

2. When Collect Stories Failed (empty data)
    - Ensure that actual result is Empty
    - Ensure that the `storyRepository.getPagedStories()` method has been called.

### SUT for `StoriesMapViewModel` - `StoriesMapViewModelTest`

1. When Map is Not Ready
    - Ensure has no interaction with `storyRepository`.

2. When Map Ready **(Load Stories Success)**
    - Ensure that result is `UiState.Success`
    - Ensure that expected result is equal with actual result
    - Ensure that the `storyRepository.getStories()` method has been called.

3. When Map Ready **(Load Stories Failed)**
    - Ensure that result is `UiState.Success`
    - Ensure that expected error is equal with actual exception
    - Ensure that the `storyRepository.getStories()` method has been called.

5. When `numLoadedStories` Change than Reload Stories
    - Ensure that `numLoadedStories` is equal with change `numLoadedStories`
    - Ensure that stories result is `UiState.Success`
    - Ensure that size of stories result is less than or equal with  `numLoadedStories`
    - Ensure that expected stories is equal with actual stories
    - Ensure that the `storyRepository.getPagedStories()` method has been called with valid parameters.

### SUT for `LoginViewModel` - `LoginViewModelTest`

1. When Login with empty input (Internal Validation) then Error
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Error`
    - Ensure that the expected result is equal with actual exception
    - Ensure has no interaction with `storyRepository`.

2. When Login  with valid input (Server Validation) then Error
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Error`
    - Ensure that expected error is equal with actual error
    - Ensure that the `storyRepository.signIn()` method has been called with given input.

3. When Login with valid input and then Success
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Success`
    - Ensure that expected user is equal with actual user
    - Ensure that the `storyRepository.signIn()` method has been called with given input.
    - Ensure that the `storyRepository.saveUserSession()` method has been called.

### SUT for `RegisterViewModel` - `RegisterViewModelTest`

1. When Register with empty input (Internal Validation) then Error
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Error`
    - Ensure that the expected result is equal with actual exception
    - Ensure has no interaction with `storyRepository`.

2. When Register with valid input (Server Validation) then Error
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Error`
    - Ensure that expected error is equal with actual error
    - Ensure that the `storyRepository.signUp()` method has been called with given input.

3. When Register with valid input and then Success
    - Ensure that `formState` is `FormState.Submit`
    - Ensure that `submitState` is `UiState.Success`
    - Ensure that expected user is equal with actual user
    - Ensure that the `storyRepository.signUp()` method has been called with given input.

### SUT for `MainViewModel`  - `MainViewModelTest`

1. When User is Login
    - Ensure that `isLogin` is `true`
    - Ensure that the `userRepository.getCurrentUser()` method has been called.

2. When User **Not** Login
    - Ensure that `isLogin` is `false`
    - Ensure that the `userRepository.getCurrentUser()` method has been called.

3. When User `logOut`
    - Ensure that the `userRepository.signOut()` method has been called.


## UI

### SUT for `HomeFragment` - `HomeFragmentTest`

1. **@LargeTest** Integrate home with detail fragment
    - make sure current destination is `homeFragment`
    - Make sure `recylerView` shown, then perform click on position 1
    - make sure current destination is `detailFragment`
    - Check and verify data that shown on`detailfragment` equal with bundle

2. Integrate home with Create Fragment **(@LargeTest)**
    - make sure current destination is `homeFragment`
    - Make sure `recylerView` shown
    - perform click on `fab`
    - make sure current destination is `createFragment`
    - Check and verify `createFragment` layout is shown

### SUT for `LoginFragment` - `LoginFragmentTest`

1. **@MediumTest** - Login with valid input  then `login button` enable
    - Ensure initial `login button` is disable
    - type valid email on Edit Text `email`
    - type valid password on Edit Text `password`
    - Ensure initial `login button` is enable

2. **@MediumTest** -  type invalid  input
    - Ensure initial `login button` is disable
    - type invalid email on Edit Text `email` then make sure error message is appear
    - type invalid password on Edit Text `password` then make sure error message is appear
    - Ensure `login button` still disable

### SUT for `RegisterFragment` - `RegisterFragmentTest`

1. **@MediumTest** - Login with valid input  then `login button` enable
    - Ensure initial `register button` is disable
    - type valid name on Edit Text `name`
    - type valid email on Edit Text `email`
    - type valid password on Edit Text `password`
    - Ensure initial`register button` is enable

2. **@MediumTest** - type invalid  input
    - Ensure initial `register button` is disable
    - type invalid email on Edit Text `email` then make sure error message is appear
    - type invalid password on Edit Text `password` then make sure error message is appear
    - Ensure `register button` still disable

### SUT for `DetailStoryFragment` - `DetailStoryFragmentTest`

1. **@MediumTest** Check view and make sure data display equal with given bundle
    - Make sure detail name is shown and text is valid
    - Make sure create at is shown and text is valid
    - Make sure description is shown and text is valid
    - Make sure Image is shown, adn have content description
