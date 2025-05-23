# MobileChat

> **Authors:**
>
> Franciszek Cieślik
>
> Kajetan Mieloch

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

[stage_1](https://github.com/user-attachments/assets/25417b74-4b48-444b-a3be-ca56edd176e2)

---

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

[stage_2.webm](https://github.com/user-attachments/assets/0637e7e8-2550-4841-a507-2eb21305c7fd)

---
     
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
