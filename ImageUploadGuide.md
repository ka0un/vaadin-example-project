# Image Upload Feature Guide

This guide walks you through how to use the image upload feature in the Vaadin Notes Application.

## Prerequisites

Before using the image upload feature, make sure you have:

1. The application running locally (see the main README.md for setup instructions)
2. A user account (registered and logged in)

## Getting Started

### 1. Register a New Account (if you don't have one)

1. Open your web browser and navigate to: [http://localhost:8080](http://localhost:8080)
2. Click on the "Register" link (if available) or navigate to [http://localhost:8080/register](http://localhost:8080/register)
3. Fill in the registration form:
   - Enter a username
   - Enter a password
   - Confirm your password
4. Click the "Register" button
5. You should be redirected to the login page

### 2. Log In

1. On the login page, enter your username and password
2. Click the "Sign In" button
3. You should now be logged in and see the main application interface

## Using the Image Upload Feature

### Accessing the Image Upload Page

1. Once logged in, look at the navigation drawer (hamburger menu on the left)
2. Click on "Images" in the navigation menu
3. This will take you to the image upload and gallery page

### Uploading Images

1. On the Images page, you'll see an upload area at the top
2. Click on the upload component or drag and drop image files onto it
3. The upload accepts image files only (image/* file types)
4. Select one or more image files from your computer
5. The upload will start automatically
6. Once uploaded, your images will be saved to the database and displayed in the gallery below

**Supported file types:** All common image formats (JPEG, PNG, GIF, BMP, etc.)

**File size limit:** Maximum 5MB per image. Files larger than this limit will be rejected with an error message.

### Viewing Uploaded Images

1. After uploading, images appear in a gallery layout below the upload area
2. Each image is displayed in a card format with:
   - The image itself (scaled to fit)
   - A "Delete" button below the image

### Deleting Images

1. To delete an image, click the "Delete" button on the image card
2. The image will be immediately removed from the gallery and database
3. This action cannot be undone

## Troubleshooting

### Upload Fails
- Make sure you're logged in
- Check that the file is actually an image
- Ensure the file size is under 5MB (files larger than this will be rejected)
- Try refreshing the page and attempting again
- Check the browser console for any error messages

### Images Don't Display
- Try refreshing the page
- Clear your browser cache
- Make sure the application is still running

### Can't Access Images Page
- Ensure you're logged in
- Check that your user account is active
- Try logging out and logging back in

## Technical Details

- Images are stored in the application's database as binary data
- Each image is associated with the user who uploaded it
- Images are displayed using Vaadin's StreamResource for efficient loading
- The gallery uses a responsive card layout with rounded corners and shadows


For more information about the application setup and other features, refer to the main README.md file.