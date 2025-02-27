# MobileChat

## Stage 1: Basic Setup and User Authentication  
### ✅ Goal: Implement a user login/registration system  
1. **Firebase Configuration**  
   - Create a project in [Firebase Console](https://console.firebase.google.com/)  
   - Integrate Firebase SDK into the project (Gradle)  
   - Configure Firebase Authentication  

2. **User Registration and Login System**  
   - Implement email/password-based user registration  
   - Implement user login  
   - Handle password reset  
   - Store user profiles in Firebase Firestore  

3. **User Session Management**  
   - Implement automatic login persistence  
   - Implement logout functionality  

## Stage 2: User Database and Data Management  
### ✅ Goal: Store user profiles and manage friend lists  
1. **Firebase Firestore Database Structure**  
   - Create a `users` collection (user ID, name, email, avatar)  
   - Create a `friends` collection to store user relationships  

2. **User Profile Editing**  
   - Allow users to update their name and avatar  
   - Handle data updates in Firestore  

3. **Friends List**  
   - Enable adding and managing friends  
   - Display available friends in the app
   - 
## Stage 3: Real-Time Chat System  
### ✅ Goal: Implement real-time messaging  
1. **Firestore Database Structure for Messages**  
   - Create a `chats` collection (chat ID, participants)  
   - Create a `messages` collection for each conversation (sender, receiver, message content, timestamp)  

2. **Sending and Receiving Messages**  
   - Implement a message input field and sending functionality  
   - Update the UI in real-time using Firebase Firestore snapshot listeners  

3. **Firebase Cloud Messaging (FCM) Notifications**  
   - Send push notifications for new messages  
   - Handle notifications in the app  
