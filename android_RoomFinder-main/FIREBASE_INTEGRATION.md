# Firebase Integration for RoomFinder App

This document explains how Firebase has been integrated into the RoomFinder Android application.

## 📋 Table of Contents
- [Overview](#overview)
- [Setup](#setup)
- [Firebase Services](#firebase-services)
- [Architecture](#architecture)
- [Usage Examples](#usage-examples)
- [Important Notes](#important-notes)

## 🔥 Overview

The RoomFinder app uses Firebase for:
- **Authentication**: User registration, login, password reset
- **Firestore**: Store room listings, user profiles, and favorites
- **Realtime Database**: Real-time chat messaging
- **Cloud Storage**: Image uploads for rooms and user profiles
- **Analytics**: Track user behavior (already configured)

## ⚙️ Setup

### 1. Prerequisites
- Firebase project created at [Firebase Console](https://console.firebase.google.com/)
- `google-services.json` file placed in `app/` directory (already done)

### 2. Dependencies
All necessary Firebase dependencies are added in `app/build.gradle.kts`:
- Firebase BOM 34.5.0
- Firebase Authentication
- Firebase Firestore
- Firebase Realtime Database
- Firebase Cloud Storage
- Firebase Cloud Messaging
- Firebase Analytics
- Google Play Services Auth (for Google Sign-In)

### 3. Firebase Rules Setup

#### Firestore Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    // Rooms collection
    match /rooms/{roomId} {
      allow read: if true; // Anyone can read rooms
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
                               request.auth.uid == resource.data.userId;
    }
    
    // Favorites collection
    match /favorites/{favoriteId} {
      allow read, write: if request.auth != null && 
                            request.auth.uid == resource.data.userId;
    }
  }
}
```

#### Realtime Database Rules
```json
{
  "rules": {
    "chats": {
      "$chatId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

#### Storage Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Users can upload their own images
    match /users/{userId}/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Rooms images
    match /rooms/{roomId}/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## 🏗️ Architecture

### Core Classes

#### 1. **FirebaseManager.java**
Singleton class that manages all Firebase services:
- Authentication operations
- Firestore CRUD operations
- Realtime Database operations
- Cloud Storage operations

#### 2. **RoomFirebaseHelper.java**
Helper class for room-specific operations:
- Add/Update/Delete rooms
- Search rooms (by location, price range)
- Manage favorites
- Upload room images

#### 3. **ChatFirebaseHelper.java**
Helper class for real-time chat:
- Create chats between users
- Send/Receive messages
- Listen for real-time updates
- Mark messages as read

#### 4. **FirebaseCallback.java**
Generic callback interface for async operations:
```java
public interface FirebaseCallback<T> {
    void onSuccess(T data);
    void onFailure(String error);
}
```

## 📝 Usage Examples

### Authentication

#### Register New User
```java
FirebaseManager firebaseManager = FirebaseManager.getInstance();

firebaseManager.registerUser(email, password, task -> {
    if (task.isSuccessful()) {
        String userId = firebaseManager.getUserId();
        // Create user profile
        firebaseManager.createUserProfile(userId, email, name,
            aVoid -> Log.d(TAG, "Profile created"),
            e -> Log.e(TAG, "Error: " + e.getMessage()));
    } else {
        Log.e(TAG, "Registration failed: " + task.getException());
    }
});
```

#### Sign In
```java
firebaseManager.signInUser(email, password, task -> {
    if (task.isSuccessful()) {
        String userId = firebaseManager.getUserId();
        // Navigate to home screen
    } else {
        // Show error
    }
});
```

#### Reset Password
```java
firebaseManager.sendPasswordResetEmail(email, task -> {
    if (task.isSuccessful()) {
        // Show success message
    }
});
```

### Room Management

#### Add New Room
```java
RoomFirebaseHelper roomHelper = new RoomFirebaseHelper();

roomHelper.addRoom(title, description, location, price, userId, imageUri,
    new FirebaseCallback<String>() {
        @Override
        public void onSuccess(String roomId) {
            // Room added successfully
        }
        
        @Override
        public void onFailure(String error) {
            // Handle error
        }
    });
```

#### Get All Rooms
```java
roomHelper.getAllRooms(new FirebaseCallback<List<Map<String, Object>>>() {
    @Override
    public void onSuccess(List<Map<String, Object>> rooms) {
        // Update UI with rooms
        for (Map<String, Object> room : rooms) {
            String title = (String) room.get("title");
            Double price = (Double) room.get("price");
            // Display in RecyclerView
        }
    }
    
    @Override
    public void onFailure(String error) {
        // Handle error
    }
});
```

#### Search Rooms by Location
```java
roomHelper.searchRoomsByLocation("New York", 
    new FirebaseCallback<List<Map<String, Object>>>() {
        @Override
        public void onSuccess(List<Map<String, Object>> rooms) {
            // Display search results
        }
        
        @Override
        public void onFailure(String error) {
            // Handle error
        }
    });
```

#### Add to Favorites
```java
String userId = firebaseManager.getUserId();
roomHelper.addToFavorites(userId, roomId, new FirebaseCallback<Void>() {
    @Override
    public void onSuccess(Void data) {
        // Show favorite icon
    }
    
    @Override
    public void onFailure(String error) {
        // Handle error
    }
});
```

### Chat

#### Create Chat
```java
ChatFirebaseHelper chatHelper = new ChatFirebaseHelper();

chatHelper.createChat(user1Id, user1Name, user2Id, user2Name,
    new FirebaseCallback<String>() {
        @Override
        public void onSuccess(String chatId) {
            // Open chat screen
        }
        
        @Override
        public void onFailure(String error) {
            // Handle error
        }
    });
```

#### Send Message
```java
chatHelper.sendMessage(chatId, senderId, senderName, message,
    new FirebaseCallback<Void>() {
        @Override
        public void onSuccess(Void data) {
            // Clear input field
        }
        
        @Override
        public void onFailure(String error) {
            // Handle error
        }
    });
```

#### Listen for Messages (Real-time)
```java
chatHelper.listenForMessages(chatId, new ChatFirebaseHelper.MessageListener() {
    @Override
    public void onMessagesReceived(List<Map<String, Object>> messages) {
        // Update chat UI
        for (Map<String, Object> message : messages) {
            String text = (String) message.get("message");
            Long timestamp = (Long) message.get("timestamp");
            // Add to RecyclerView
        }
    }
    
    @Override
    public void onError(String error) {
        // Handle error
    }
});
```

### Image Upload

#### Upload Room Image
```java
FirebaseManager firebaseManager = FirebaseManager.getInstance();
String storagePath = "rooms/" + roomId + "/image.jpg";

firebaseManager.uploadImageAndGetUrl(imageUri, storagePath,
    downloadUri -> {
        String imageUrl = downloadUri.toString();
        // Save URL to Firestore
    },
    e -> {
        // Handle error
    });
```

## ⚠️ Important Notes

### 1. Permissions
The following permissions are added in `AndroidManifest.xml`:
- `INTERNET` - Required for Firebase
- `ACCESS_NETWORK_STATE` - Check network connectivity
- `CAMERA` - Take photos
- `READ_EXTERNAL_STORAGE` - Pick images
- `READ_MEDIA_IMAGES` - Android 13+ image access

### 2. Initialization
Firebase is automatically initialized in `MainActivity.onCreate()`. The initialization includes:
- FirebaseApp initialization
- FirebaseManager singleton instance
- Connection status check
- User authentication state check

### 3. Error Handling
Always implement both `onSuccess` and `onFailure` callbacks:
```java
new FirebaseCallback<T>() {
    @Override
    public void onSuccess(T data) {
        // Handle success
    }
    
    @Override
    public void onFailure(String error) {
        // Always handle errors and show user-friendly messages
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
    }
}
```

### 4. User Authentication State
Check if user is logged in before performing operations:
```java
if (firebaseManager.isUserLoggedIn()) {
    String userId = firebaseManager.getUserId();
    // Perform authenticated operations
} else {
    // Redirect to login screen
}
```

### 5. Data Structure

#### User Document (Firestore)
```json
{
  "userId": "string",
  "email": "string",
  "name": "string",
  "createdAt": timestamp
}
```

#### Room Document (Firestore)
```json
{
  "title": "string",
  "description": "string",
  "location": "string",
  "price": number,
  "userId": "string",
  "imageUrl": "string",
  "createdAt": timestamp,
  "status": "available"
}
```

#### Chat Node (Realtime Database)
```json
{
  "chatId": "string",
  "user1Id": "string",
  "user1Name": "string",
  "user2Id": "string",
  "user2Name": "string",
  "lastMessage": "string",
  "lastMessageTime": timestamp,
  "messages": {
    "messageId": {
      "senderId": "string",
      "senderName": "string",
      "message": "string",
      "timestamp": timestamp,
      "read": boolean
    }
  }
}
```

## 🚀 Next Steps

1. **Enable Firebase Services** in Firebase Console:
   - Authentication (Email/Password, Google Sign-In)
   - Firestore Database
   - Realtime Database
   - Cloud Storage
   - Cloud Messaging (for notifications)

2. **Configure Security Rules** as shown above

3. **Test the Integration**:
   - Run the app and check logcat for "Firebase connected successfully"
   - Try registering a new user
   - Test CRUD operations

4. **Implement UI** to use these Firebase helpers in your activities

## 📚 Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Android Codelab](https://firebase.google.com/codelabs/firebase-android)
- [Firestore Data Modeling](https://firebase.google.com/docs/firestore/data-model)
- [Firebase Authentication](https://firebase.google.com/docs/auth)

## 🐛 Troubleshooting

1. **"Firebase not initialized"** - Make sure `google-services.json` is in the `app/` directory
2. **Authentication fails** - Enable Email/Password authentication in Firebase Console
3. **Permission denied** - Check Firestore/Database security rules
4. **Image upload fails** - Check Storage security rules and permissions

For complete usage examples, see `FirebaseUsageExample.java`.
