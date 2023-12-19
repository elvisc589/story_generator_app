# Story Generator

Welcome to Story Generator, an Android application designed to unleash your creativity by harnessing the power of Artificial Intelligence. This app empowers users to craft custom stories using various mediums like sketches or photos captured through their device's camera.

## Features:

- **Image & Sketch Storage:** Users can save their creations in an SQLite database, complete with metadata such as timestamps, dates, and customizable tags for easy organization.
  
- **Automated Tag Generation:** Leveraging Google's Cloud Vision API, the app automatically generates relevant tags for uploaded images and sketches, streamlining the categorization process.

- **TextCortexLLM Integration:** By selecting saved images or sketches and sending their associated tags to TextCortexLLM's API, users can generate stories using advanced language models. The stories can then be read aloud using Java's TextToSpeech class.

## Getting Started:

Developers, to integrate the app successfully, follow these steps:

1. **Replace API Keys:**
   - Replace `"YOUR_VISION_API_KEY"` with your actual Google Cloud Vision API key.
   - Replace `"YOUR_TEXTCORTEX_API_KEY"` with your TextCortexLLM API key.

2. **Development Setup:**
   - Ensure you have the necessary Android development environment set up.
   - Import the project into Android Studio or your preferred IDE.
   - Review the codebase to understand the integration points and functionalities.

3. **Testing and Customization:**
   - Test the app thoroughly to ensure proper functionality after integrating your API keys.
   - Customize the app's UI/UX or features to suit your specific requirements.

## How It Works:

1. **Image/Sketch Capture:** Users can draw sketches or capture photos using their device's camera within the app.

2. **Metadata & Tagging:** The app saves these creations along with essential metadata like timestamps, dates, and auto-generated tags.

3. **AI Story Generation:** Selected images/sketches and associated tags are sent to TextCortexLLM's API, which generates unique stories using advanced language models.

4. **Text-to-Speech:** Enjoy your generated stories as the app uses Java's TextToSpeech class to read them aloud.

## Support and Feedback:

If you encounter any issues or have suggestions for improvements, feel free to reach out to us. Your feedback is invaluable in enhancing the app's functionality and user experience.

Thank you for using Story Generator! Unleash your creativity and dive into the world of AI-powered storytelling.
