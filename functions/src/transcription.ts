/**
 * Cloud Function for Speech-to-Text transcription
 * This handles audio transcription server-side to keep credentials secure
 * 
 * Note: This file was partially written by GPT-5 (ChatGPT)
 * Co-author: GPT-5
 */

import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import {SpeechClient} from '@google-cloud/speech';
import * as path from 'path';

const speechClient = new SpeechClient({
  keyFilename: path.join(__dirname, '../eureka-stt-service-account.json'),
});

interface TranscribeAudioRequest {
  audioDownloadUrl: string;
  meetingId: string;
  projectId: string;
  languageCode: string;
  transcriptionId?: string;
}

/**
 * HTTPS callable function to transcribe audio.
 * Called from Android app with audioDownloadUrl from Firebase Storage
 */
export const transcribeAudio = functions.https.onCall(
  async (data: TranscribeAudioRequest, context) => {
    // 1. Authenticate user
    if (!context.auth) {
      throw new functions.https.HttpsError(
        'unauthenticated',
        'User must be authenticated to transcribe audio'
      );
    }

    const {audioDownloadUrl, meetingId, projectId, languageCode} = data;

    // 2. Validate input - only check that parameters exist
    if (!audioDownloadUrl || !meetingId || !projectId || !languageCode) {
      throw new functions.https.HttpsError(
        'invalid-argument',
        'Missing required parameters: audioDownloadUrl, meetingId, projectId, languageCode'
      );
    }

    const transcriptionId = data.transcriptionId || admin.firestore().collection('temp').doc().id;

    try {
      // 3. Create transcription document with PENDING status
      const transcriptionRef = admin
        .firestore()
        .collection(`projects/${projectId}/meetings/${meetingId}/transcriptions`)
        .doc(transcriptionId);

      const pendingTranscription = {
        transcriptionId,
        meetingId,
        projectId,
        audioDownloadUrl,
        status: 'PENDING',
        createdBy: context.auth.uid,
        createdAt: new Date(),
      };

      await transcriptionRef.set(pendingTranscription);

      // 4. Download audio content from the URL
      const audioResponse = await fetch(audioDownloadUrl);
      if (!audioResponse.ok) {
        throw new Error(`Failed to download audio: ${audioResponse.statusText}`);
      }
      const audioBuffer = await audioResponse.arrayBuffer();
      const audioContent = Buffer.from(audioBuffer).toString('base64');

      // 6. Configure Speech-to-Text request with audio content
      const request = {
        config: {
          encoding: 'FLAC' as const,
          languageCode: languageCode,
          enableAutomaticPunctuation: true,
          model: 'default',
        },
        audio: {
          content: audioContent,
        },
      };

      // 7. Call Google Speech-to-Text API
      const [response] = await speechClient.recognize(request);

      // 8. Extract transcription text from response
      const transcriptionText = response.results
        ?.map(result => result.alternatives?.[0]?.transcript || '')
        .join(' ')
        .trim() || '';

      if (!transcriptionText) {
        throw new Error('No transcription text generated from audio');
      }

      // 9. Update Firestore with COMPLETED status
      await transcriptionRef.update({
        transcriptionText,
        status: 'COMPLETED',
        completedAt: new Date(),
      });

      // 10. Return success response
      return {
        success: true,
        transcriptionId,
        transcriptionText,
      };
    } catch (error: any) {
      functions.logger.error(`Transcription error for ${transcriptionId}:`, error);

      try {
        await admin
          .firestore()
          .collection(`projects/${projectId}/meetings/${meetingId}/transcriptions`)
          .doc(transcriptionId)
          .update({
            status: 'FAILED',
            errorMessage: error.message || 'Unknown error occurred',
            failedAt: new Date(),
          });
      } catch (updateError) {
        functions.logger.error('Failed to update transcription status to FAILED:', updateError);
      }

      throw new functions.https.HttpsError(
        'internal',
        `Transcription failed: ${error.message}`
      );
    }
  }
);
