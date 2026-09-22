import firebaseConfig from '../../firebase-applet-config.json';
import { auth, googleProvider } from '../firebase/config';
import { signInWithPopup, GoogleAuthProvider } from 'firebase/auth';
import { notifyWorkspaceSyncUpdated } from './googleSheetsSync';

declare global {
  interface Window {
    google?: any;
  }
}

export interface GoogleDriveFile {
  id: string;
  name: string;
  mimeType: string;
  size?: string;
  createdTime: string;
  modifiedTime?: string;
  description?: string;
  webViewLink?: string;
}

export interface DriveTokenInfo {
  token: string;
  expiresAt: number;
  userEmail?: string;
}

const BACKUP_FOLDER_NAME = 'Falcon Rod Maker POS - Database Backups';
const DRIVE_SCOPE = 'https://www.googleapis.com/auth/drive.file';
const STORAGE_KEY_TOKEN = 'falcon_gdrive_token';
const STORAGE_KEY_EXPIRES = 'falcon_gdrive_expires_at';
const STORAGE_KEY_EMAIL = 'falcon_gdrive_email';
const STORAGE_KEY_FOLDER_ID = 'falcon_gdrive_folder_id';
const STORAGE_KEY_LAST_BACKUP = 'falcon_gdrive_last_backup_time';
const STORAGE_KEY_AUTO_BACKUP = 'falcon_gdrive_autobackup';

/**
 * Retrieve cached OAuth access token if still valid
 */
export function getStoredDriveToken(): DriveTokenInfo | null {
  try {
    const token = localStorage.getItem(STORAGE_KEY_TOKEN);
    const expiresStr = localStorage.getItem(STORAGE_KEY_EXPIRES);
    const userEmail = localStorage.getItem(STORAGE_KEY_EMAIL) || undefined;
    
    if (!token || !expiresStr) return null;
    
    const expiresAt = parseInt(expiresStr, 10);
    // Buffer by 2 minutes
    if (Date.now() > expiresAt - 120000) {
      return null;
    }
    
    return { token, expiresAt, userEmail };
  } catch {
    return null;
  }
}

/**
 * Persist access token to localStorage
 */
export function storeDriveToken(token: string, expiresInSeconds: number, email?: string): void {
  try {
    const expiresAt = Date.now() + (expiresInSeconds * 1000);
    localStorage.setItem(STORAGE_KEY_TOKEN, token);
    localStorage.setItem(STORAGE_KEY_EXPIRES, expiresAt.toString());
    if (email) {
      localStorage.setItem(STORAGE_KEY_EMAIL, email);
    }
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store Drive token in localStorage', err);
  }
}

/**
 * Clear cached Google Drive credentials
 */
export function clearDriveToken(): void {
  try {
    localStorage.removeItem(STORAGE_KEY_TOKEN);
    localStorage.removeItem(STORAGE_KEY_EXPIRES);
    localStorage.removeItem(STORAGE_KEY_EMAIL);
    localStorage.removeItem(STORAGE_KEY_FOLDER_ID);
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to clear Drive token', err);
  }
}

export function getStoredDriveFolderId(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY_FOLDER_ID);
  } catch {
    return null;
  }
}

export function getLastDriveBackupTime(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY_LAST_BACKUP);
  } catch {
    return null;
  }
}

export function setLastDriveBackupTime(time: string): void {
  try {
    localStorage.setItem(STORAGE_KEY_LAST_BACKUP, time);
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store last Drive backup time', err);
  }
}

export function getDriveAutoBackupEnabled(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY_AUTO_BACKUP) === 'true';
  } catch {
    return false;
  }
}

export function setDriveAutoBackupEnabled(enabled: boolean): void {
  try {
    localStorage.setItem(STORAGE_KEY_AUTO_BACKUP, enabled ? 'true' : 'false');
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store Drive auto backup setting', err);
  }
}

/**
 * Acquire Google Drive OAuth token using Google Identity Services (GSI)
 * or fallback to Firebase Auth Popup with Drive scope.
 */
export async function requestGoogleDriveToken(preferredEmail?: string): Promise<string> {
  const clientId = firebaseConfig.oAuthClientId;

  // Try GSI Token Client if available in window.google
  if (typeof window !== 'undefined' && window.google?.accounts?.oauth2 && clientId) {
    try {
      const token = await new Promise<string>((resolve, reject) => {
        const tokenClient = window.google.accounts.oauth2.initTokenClient({
          client_id: clientId,
          scope: DRIVE_SCOPE,
          hint: preferredEmail || 'umarzaman7777777@gmail.com',
          prompt: '',
          callback: (response: any) => {
            if (response.error) {
              reject(new Error(response.error_description || response.error));
            } else if (response.access_token) {
              const expiresIn = response.expires_in ? parseInt(response.expires_in, 10) : 3500;
              storeDriveToken(response.access_token, expiresIn, preferredEmail || 'umarzaman7777777@gmail.com');
              resolve(response.access_token);
            } else {
              reject(new Error('No access token returned by Google Identity Services'));
            }
          },
        });
        tokenClient.requestAccessToken();
      });
      return token;
    } catch (gsiErr) {
      console.warn('GSI initTokenClient attempt encountered issue, falling back to Firebase Auth Popup:', gsiErr);
    }
  }

  // Fallback: Firebase Auth with GoogleAuthProvider adding drive.file scope
  try {
    const provider = new GoogleAuthProvider();
    provider.addScope(DRIVE_SCOPE);
    provider.setCustomParameters({
      prompt: 'select_account',
      login_hint: preferredEmail || 'umarzaman7777777@gmail.com'
    });

    const result = await signInWithPopup(auth, provider);
    const credential = GoogleAuthProvider.credentialFromResult(result);
    const accessToken = credential?.accessToken;

    if (!accessToken) {
      throw new Error('Could not acquire Google Drive access token from authentication result.');
    }

    storeDriveToken(accessToken, 3500, result.user.email || preferredEmail || 'umarzaman7777777@gmail.com');
    return accessToken;
  } catch (error: any) {
    throw new Error(error?.message || 'Authentication with Google Drive was cancelled or failed.');
  }
}

/**
 * Locate or create the dedicated backup folder in the user's Google Drive
 */
export async function getOrCreateBackupFolder(accessToken: string): Promise<{ id: string; name: string }> {
  // Check cached folder ID first
  const cachedFolderId = localStorage.getItem(STORAGE_KEY_FOLDER_ID);
  if (cachedFolderId) {
    try {
      const verifyRes = await fetch(`https://www.googleapis.com/drive/v3/files/${cachedFolderId}?fields=id,name,trashed`, {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
      if (verifyRes.ok) {
        const folder = await verifyRes.json();
        if (!folder.trashed) {
          return { id: folder.id, name: folder.name };
        }
      }
    } catch {
      // Cached ID was stale or invalid, will re-query
    }
  }

  // Query for existing folder
  const query = encodeURIComponent(`name = '${BACKUP_FOLDER_NAME}' and mimeType = 'application/vnd.google-apps.folder' and trashed = false`);
  const searchRes = await fetch(`https://www.googleapis.com/drive/v3/files?q=${query}&fields=files(id,name)&spaces=drive`, {
    headers: { Authorization: `Bearer ${accessToken}` }
  });

  if (!searchRes.ok) {
    const errText = await searchRes.text();
    throw new Error(`Failed to query Google Drive folder: ${searchRes.status} ${errText}`);
  }

  const searchData = await searchRes.json();
  if (searchData.files && searchData.files.length > 0) {
    const folder = searchData.files[0];
    localStorage.setItem(STORAGE_KEY_FOLDER_ID, folder.id);
    return { id: folder.id, name: folder.name };
  }

  // Create new folder
  const createRes = await fetch('https://www.googleapis.com/drive/v3/files', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name: BACKUP_FOLDER_NAME,
      mimeType: 'application/vnd.google-apps.folder',
      description: 'Automated and manual database snapshots for Falcon Rod Maker POS & ERP'
    })
  });

  if (!createRes.ok) {
    const errText = await createRes.text();
    throw new Error(`Failed to create Google Drive backup folder: ${createRes.status} ${errText}`);
  }

  const newFolder = await createRes.json();
  localStorage.setItem(STORAGE_KEY_FOLDER_ID, newFolder.id);
  return { id: newFolder.id, name: newFolder.name };
}

/**
 * Upload a JSON or SQL backup file into the Google Drive backup folder
 */
export async function uploadBackupToGoogleDrive(
  accessToken: string,
  options: {
    fileName: string;
    fileContent: string;
    mimeType: string;
    description?: string;
  }
): Promise<GoogleDriveFile> {
  const folder = await getOrCreateBackupFolder(accessToken);

  const metadata = {
    name: options.fileName,
    description: options.description || 'Falcon Rod Maker POS Database Backup',
    mimeType: options.mimeType,
    parents: [folder.id]
  };

  const boundary = '-------314159265358979323846';
  const delimiter = `\r\n--${boundary}\r\n`;
  const closeDelimiter = `\r\n--${boundary}--`;

  const multipartRequestBody =
    delimiter +
    'Content-Type: application/json; charset=UTF-8\r\n\r\n' +
    JSON.stringify(metadata) +
    delimiter +
    `Content-Type: ${options.mimeType}; charset=UTF-8\r\n\r\n` +
    options.fileContent +
    closeDelimiter;

  const uploadRes = await fetch('https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,mimeType,size,createdTime,modifiedTime,description,webViewLink', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': `multipart/related; boundary=${boundary}`
    },
    body: multipartRequestBody
  });

  if (!uploadRes.ok) {
    const errText = await uploadRes.text();
    throw new Error(`Google Drive upload failed: ${uploadRes.status} ${errText}`);
  }

  const uploadedFile: GoogleDriveFile = await uploadRes.json();
  setLastDriveBackupTime(new Date().toISOString());
  return uploadedFile;
}

/**
 * Fetch list of all database backups present in the Google Drive folder
 */
export async function listGoogleDriveBackups(accessToken: string): Promise<{ files: GoogleDriveFile[]; folderId: string }> {
  const folder = await getOrCreateBackupFolder(accessToken);

  const query = encodeURIComponent(`'${folder.id}' in parents and trashed = false`);
  const listRes = await fetch(
    `https://www.googleapis.com/drive/v3/files?q=${query}&fields=files(id,name,mimeType,size,createdTime,modifiedTime,description,webViewLink)&orderBy=createdTime desc&pageSize=50`,
    {
      headers: { Authorization: `Bearer ${accessToken}` }
    }
  );

  if (!listRes.ok) {
    const errText = await listRes.text();
    throw new Error(`Failed to list backups from Google Drive: ${listRes.status} ${errText}`);
  }

  const data = await listRes.json();
  return {
    files: data.files || [],
    folderId: folder.id
  };
}

/**
 * Download the raw content of a specific backup file from Google Drive
 */
export async function downloadGoogleDriveBackupContent(accessToken: string, fileId: string): Promise<string> {
  const res = await fetch(`https://www.googleapis.com/drive/v3/files/${fileId}?alt=media`, {
    headers: { Authorization: `Bearer ${accessToken}` }
  });

  if (!res.ok) {
    const errText = await res.text();
    throw new Error(`Failed to download backup from Google Drive: ${res.status} ${errText}`);
  }

  return await res.text();
}

/**
 * Delete a backup file from Google Drive
 */
export async function deleteGoogleDriveBackup(accessToken: string, fileId: string): Promise<void> {
  const res = await fetch(`https://www.googleapis.com/drive/v3/files/${fileId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${accessToken}` }
  });

  if (!res.ok && res.status !== 204) {
    const errText = await res.text();
    throw new Error(`Failed to delete backup from Google Drive: ${res.status} ${errText}`);
  }
}

/**
 * Utility: format byte sizes
 */
export function formatDriveFileSize(bytes?: number | string): string {
  if (!bytes) return '0 KB';
  const num = typeof bytes === 'string' ? parseInt(bytes, 10) : bytes;
  if (isNaN(num)) return '0 KB';
  if (num < 1024) return `${num} B`;
  if (num < 1024 * 1024) return `${(num / 1024).toFixed(1)} KB`;
  return `${(num / (1024 * 1024)).toFixed(2)} MB`;
}

/**
 * Utility: format ISO timestamp to human-friendly workshop date
 */
export function formatDriveDate(isoDateString?: string): string {
  if (!isoDateString) return 'Unknown date';
  try {
    const d = new Date(isoDateString);
    return d.toLocaleString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch {
    return isoDateString;
  }
}
