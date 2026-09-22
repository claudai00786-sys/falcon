import { useState, useEffect, useCallback, useRef } from 'react';
import { AppState, Transaction } from '../types';
import {
  getStoredSheetsToken,
  getStoredSpreadsheetId,
  getStoredSpreadsheetTitle,
  getStoredSpreadsheetUrl,
  getLastSheetsSyncTime,
  getSheetsAutoSyncEnabled,
  setSheetsAutoSyncEnabled,
  clearSheetsToken,
  requestGoogleSheetsToken,
  populateAllSheets,
  syncSingleTransactionWithSheet,
  WORKSPACE_SYNC_EVENT,
  notifyWorkspaceSyncUpdated,
  ALLOWED_SHEETS_OWNER_EMAIL,
  extractSpreadsheetId
} from '../utils/googleSheetsSync';
import {
  getStoredDriveToken,
  getStoredDriveFolderId,
  getLastDriveBackupTime,
  getDriveAutoBackupEnabled,
  setDriveAutoBackupEnabled,
  clearDriveToken,
  requestGoogleDriveToken,
  uploadBackupToGoogleDrive,
  listGoogleDriveBackups
} from '../utils/googleDriveBackup';
import { getDatabaseJSONString } from '../utils/syncReport';
import { generateFullDatabaseSQL } from '../utils/sqlExporter';
import firebaseConfig from '../../firebase-applet-config.json';
import { auth } from '../firebase/config';

export interface WorkspaceSyncState {
  // Google Sheets
  sheetsConnected: boolean;
  sheetsToken: string | null;
  spreadsheetId: string | null;
  spreadsheetTitle: string | null;
  spreadsheetUrl: string | null;
  lastSheetsSync: string | null;
  sheetsAutoSync: boolean;
  isSyncingSheets: boolean;
  sheetsError: string | null;
  sheetsSuccessMsg: string | null;

  // Google Drive
  driveConnected: boolean;
  driveToken: string | null;
  driveFolderId: string | null;
  lastDriveBackup: string | null;
  driveAutoBackup: boolean;
  isUploadingDrive: boolean;
  driveError: string | null;
  driveSuccessMsg: string | null;
  driveBackupsCount: number;

  // Combined Status for Header Indicator
  workspaceStatus: 'syncing' | 'synced' | 'ready' | 'warning' | 'disconnected';
  statusBadgeText: string;
  statusBadgeTooltip: string;
}

export function useWorkspaceSync(appState: AppState, terminalId: string = 'default_terminal') {
  // Sheets state
  const [sheetsTokenInfo, setSheetsTokenInfo] = useState(() => getStoredSheetsToken());
  const [spreadsheetId, setSpreadsheetId] = useState<string | null>(() => getStoredSpreadsheetId());
  const [spreadsheetTitle, setSpreadsheetTitle] = useState<string | null>(() => getStoredSpreadsheetTitle());
  const [spreadsheetUrl, setSpreadsheetUrl] = useState<string | null>(() => getStoredSpreadsheetUrl());
  const [lastSheetsSync, setLastSheetsSync] = useState<string | null>(() => getLastSheetsSyncTime());
  const [sheetsAutoSync, setSheetsAutoSync] = useState<boolean>(() => getSheetsAutoSyncEnabled());
  const [isSyncingSheets, setIsSyncingSheets] = useState(false);
  const [sheetsError, setSheetsError] = useState<string | null>(null);
  const [sheetsSuccessMsg, setSheetsSuccessMsg] = useState<string | null>(null);

  // Drive state
  const [driveTokenInfo, setDriveTokenInfo] = useState(() => getStoredDriveToken());
  const [driveFolderId, setDriveFolderId] = useState<string | null>(() => getStoredDriveFolderId());
  const [lastDriveBackup, setLastDriveBackup] = useState<string | null>(() => getLastDriveBackupTime());
  const [driveAutoBackup, setDriveAutoBackup] = useState<boolean>(() => getDriveAutoBackupEnabled());
  const [isUploadingDrive, setIsUploadingDrive] = useState(false);
  const [driveError, setDriveError] = useState<string | null>(null);
  const [driveSuccessMsg, setDriveSuccessMsg] = useState<string | null>(null);
  const [driveBackupsCount, setDriveBackupsCount] = useState<number>(0);

  const initialBackupChecked = useRef(false);
  const currentUserEmail = auth.currentUser?.email || ALLOWED_SHEETS_OWNER_EMAIL;

  // Refresh local state from storage
  const refreshWorkspaceState = useCallback(() => {
    const sToken = getStoredSheetsToken();
    setSheetsTokenInfo(sToken);
    setSpreadsheetId(getStoredSpreadsheetId());
    setSpreadsheetTitle(getStoredSpreadsheetTitle());
    setSpreadsheetUrl(getStoredSpreadsheetUrl());
    setLastSheetsSync(getLastSheetsSyncTime());
    setSheetsAutoSync(getSheetsAutoSyncEnabled());

    const dToken = getStoredDriveToken();
    setDriveTokenInfo(dToken);
    setDriveFolderId(getStoredDriveFolderId());
    setLastDriveBackup(getLastDriveBackupTime());
    setDriveAutoBackup(getDriveAutoBackupEnabled());
  }, []);

  // Listen to custom workspace sync events and storage changes
  useEffect(() => {
    const handleUpdate = () => refreshWorkspaceState();
    window.addEventListener(WORKSPACE_SYNC_EVENT, handleUpdate);
    window.addEventListener('storage', handleUpdate);
    window.addEventListener('focus', handleUpdate);

    return () => {
      window.removeEventListener(WORKSPACE_SYNC_EVENT, handleUpdate);
      window.removeEventListener('storage', handleUpdate);
      window.removeEventListener('focus', handleUpdate);
    };
  }, [refreshWorkspaceState]);

  // Check Drive backups count if token exists
  useEffect(() => {
    if (driveTokenInfo?.token) {
      listGoogleDriveBackups(driveTokenInfo.token)
        .then(res => {
          setDriveBackupsCount(res.files.length);
          if (res.folderId) setDriveFolderId(res.folderId);
        })
        .catch(err => {
          console.warn('Silent drive list check:', err);
        });
    }
  }, [driveTokenInfo?.token]);

  // Automated session backup to Google Drive
  useEffect(() => {
    if (initialBackupChecked.current) return;
    initialBackupChecked.current = true;

    if (driveAutoBackup && driveTokenInfo?.token) {
      const lastBackupStr = getLastDriveBackupTime();
      let shouldBackup = true;

      if (lastBackupStr) {
        const lastTime = new Date(lastBackupStr).getTime();
        // If backed up within the last 6 hours, skip auto backup
        if (!isNaN(lastTime) && Date.now() - lastTime < 6 * 60 * 60 * 1000) {
          shouldBackup = false;
        }
      }

      if (shouldBackup) {
        console.log('Initiating automated session Google Drive snapshot backup...');
        const { jsonString, filename } = getDatabaseJSONString(appState, currentUserEmail, terminalId, true);
        uploadBackupToGoogleDrive(driveTokenInfo.token, {
          fileName: filename,
          fileContent: jsonString,
          mimeType: 'application/json',
          description: `Falcon Rod Maker POS - Automated Session Backup (${appState.transactions.length} txns, ${appState.products.length} products)`
        })
          .then(file => {
            console.log('Automated Google Drive session backup successful:', file.name);
            setDriveSuccessMsg(`✓ Auto-backup saved to Google Drive: ${file.name}`);
            setDriveBackupsCount(prev => prev + 1);
            setLastDriveBackup(new Date().toISOString());
            setTimeout(() => setDriveSuccessMsg(null), 5000);
          })
          .catch(err => {
            console.warn('Automated Drive session backup encountered error:', err);
          });
      }
    }
  }, [driveAutoBackup, driveTokenInfo?.token, appState, currentUserEmail, terminalId]);

  // Debounced auto-sync for Google Sheets when enabled and data changes
  const lastSyncedHashRef = useRef<string>('');
  useEffect(() => {
    if (!sheetsAutoSync || !sheetsTokenInfo?.token || !spreadsheetId) return;

    const currentHash = `${appState.transactions?.length || 0}_${appState.customerPayments?.length || 0}_${appState.products?.length || 0}_${appState.rawStock?.length || 0}_${appState.expenses?.length || 0}`;

    // Initialize hash on first run
    if (!lastSyncedHashRef.current) {
      lastSyncedHashRef.current = currentHash;
      return;
    }

    if (lastSyncedHashRef.current === currentHash) return;

    const timer = setTimeout(() => {
      lastSyncedHashRef.current = currentHash;
      console.log('Debounced Google Sheets auto-sync triggered for updated workshop state...');
      syncAllSheets();
    }, 6000);

    return () => clearTimeout(timer);
  }, [sheetsAutoSync, sheetsTokenInfo?.token, spreadsheetId, appState]);

  // Sync All 7 Google Sheets
  const syncAllSheets = useCallback(
    async (customState?: AppState): Promise<boolean> => {
      const token = sheetsTokenInfo?.token || getStoredSheetsToken()?.token;
      const targetId = spreadsheetId || getStoredSpreadsheetId();

      if (!token) {
        setSheetsError('Please connect your Google Account first to authorize Sheets.');
        return false;
      }
      if (!targetId) {
        setSheetsError('Please link or create a Google Spreadsheet first in the Sheets panel.');
        return false;
      }

      setIsSyncingSheets(true);
      setSheetsError(null);
      try {
        const stateToSync = customState || appState;
        await populateAllSheets(token, targetId, stateToSync);
        const now = new Date().toLocaleString('en-GB');
        setLastSheetsSync(now);
        setSheetsSuccessMsg('✓ All 7 Google Sheets tabs updated successfully!');
        setTimeout(() => setSheetsSuccessMsg(null), 4000);
        notifyWorkspaceSyncUpdated();
        return true;
      } catch (err: any) {
        if (err?.message?.includes('401') || err?.message?.includes('Invalid Credentials')) {
          clearSheetsToken();
          setSheetsTokenInfo(null);
          setSheetsError('Google Sheets authorization expired. Please reconnect your account.');
        } else {
          setSheetsError(err?.message || 'Failed to sync with Google Sheets.');
        }
        return false;
      } finally {
        setIsSyncingSheets(false);
      }
    },
    [sheetsTokenInfo, spreadsheetId, appState]
  );

  // Sync a single transaction to Google Sheets (fast append + dashboard update)
  const syncTransactionToSheets = useCallback(
    async (transaction: Transaction, latestState?: AppState): Promise<boolean> => {
      const token = sheetsTokenInfo?.token || getStoredSheetsToken()?.token;
      const targetId = spreadsheetId || getStoredSpreadsheetId();
      const isAuto = sheetsAutoSync || getSheetsAutoSyncEnabled();

      if (!isAuto || !token || !targetId) {
        return false;
      }

      try {
        await syncSingleTransactionWithSheet(token, targetId, transaction, latestState || appState);
        const now = new Date().toLocaleString('en-GB');
        setLastSheetsSync(now);
        return true;
      } catch (err: any) {
        console.warn('Non-blocking transaction Google Sheet sync notice:', err);
        return false;
      }
    },
    [sheetsTokenInfo, spreadsheetId, sheetsAutoSync, appState]
  );

  // Upload Database Snapshot to Google Drive
  const backupToDrive = useCallback(
    async (format: 'json' | 'sql' = 'json', customState?: AppState): Promise<boolean> => {
      const token = driveTokenInfo?.token || getStoredDriveToken()?.token;
      if (!token) {
        setDriveError('Please connect Google Drive first to create cloud backups.');
        return false;
      }

      setIsUploadingDrive(true);
      setDriveError(null);
      try {
        const stateToBackup = customState || appState;
        if (format === 'json') {
          const { jsonString, filename } = getDatabaseJSONString(stateToBackup, currentUserEmail, terminalId, true);
          const uploaded = await uploadBackupToGoogleDrive(token, {
            fileName: filename,
            fileContent: jsonString,
            mimeType: 'application/json',
            description: `Falcon Rod Maker POS Full JSON Database Snapshot (${stateToBackup.transactions.length} txns, ${stateToBackup.products.length} products)`
          });
          setDriveSuccessMsg(`✓ Successfully backed up JSON database to Google Drive: ${uploaded.name}`);
        } else {
          const sql = generateFullDatabaseSQL(
            stateToBackup,
            firebaseConfig.projectId,
            firebaseConfig.firestoreDatabaseId
          );
          const nowStr = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
          const filename = `Falcon_POS_PostgreSQL_Dump_${nowStr}.sql`;
          const uploaded = await uploadBackupToGoogleDrive(token, {
            fileName: filename,
            fileContent: sql,
            mimeType: 'application/sql',
            description: `Falcon Rod Maker POS Full PostgreSQL Schema & Data Dump`
          });
          setDriveSuccessMsg(`✓ Successfully uploaded SQL database dump to Google Drive: ${uploaded.name}`);
        }

        const now = new Date().toISOString();
        setLastDriveBackup(now);
        setDriveBackupsCount(prev => prev + 1);
        setTimeout(() => setDriveSuccessMsg(null), 5000);
        notifyWorkspaceSyncUpdated();
        return true;
      } catch (err: any) {
        if (err?.message?.includes('401') || err?.message?.includes('Invalid Credentials')) {
          clearDriveToken();
          setDriveTokenInfo(null);
          setDriveError('Google Drive authorization expired. Please reconnect your account.');
        } else {
          setDriveError(err?.message || 'Failed to upload backup to Google Drive.');
        }
        return false;
      } finally {
        setIsUploadingDrive(false);
      }
    },
    [driveTokenInfo, appState, currentUserEmail, terminalId]
  );

  // Connect Sheets
  const connectSheets = useCallback(async (): Promise<string | null> => {
    setSheetsError(null);
    try {
      const token = await requestGoogleSheetsToken(currentUserEmail);
      setSheetsTokenInfo(getStoredSheetsToken());
      setSheetsSuccessMsg('✓ Google Sheets connected successfully!');
      setTimeout(() => setSheetsSuccessMsg(null), 4000);
      notifyWorkspaceSyncUpdated();
      return token;
    } catch (err: any) {
      setSheetsError(err?.message || 'Failed to connect Google Sheets.');
      return null;
    }
  }, [currentUserEmail]);

  // Connect Drive
  const connectDrive = useCallback(async (): Promise<string | null> => {
    setDriveError(null);
    try {
      const token = await requestGoogleDriveToken(currentUserEmail);
      setDriveTokenInfo(getStoredDriveToken());
      setDriveSuccessMsg('✓ Google Drive connected successfully!');
      setTimeout(() => setDriveSuccessMsg(null), 4000);
      notifyWorkspaceSyncUpdated();
      return token;
    } catch (err: any) {
      setDriveError(err?.message || 'Failed to connect Google Drive.');
      return null;
    }
  }, [currentUserEmail]);

  // Disconnect Sheets
  const disconnectSheets = useCallback(() => {
    clearSheetsToken();
    setSheetsTokenInfo(null);
    setSheetsSuccessMsg('Google Sheets authorization disconnected.');
    setTimeout(() => setSheetsSuccessMsg(null), 3000);
    notifyWorkspaceSyncUpdated();
  }, []);

  // Disconnect Drive
  const disconnectDrive = useCallback(() => {
    clearDriveToken();
    setDriveTokenInfo(null);
    setDriveSuccessMsg('Google Drive session disconnected.');
    setTimeout(() => setDriveSuccessMsg(null), 3000);
    notifyWorkspaceSyncUpdated();
  }, []);

  // Toggle Sheets Auto Sync
  const toggleSheetsAutoSync = useCallback((enabled?: boolean) => {
    const newVal = enabled !== undefined ? enabled : !sheetsAutoSync;
    setSheetsAutoSync(newVal);
    setSheetsAutoSyncEnabled(newVal);
  }, [sheetsAutoSync]);

  // Toggle Drive Auto Backup
  const toggleDriveAutoBackup = useCallback((enabled?: boolean) => {
    const newVal = enabled !== undefined ? enabled : !driveAutoBackup;
    setDriveAutoBackup(newVal);
    setDriveAutoBackupEnabled(newVal);
  }, [driveAutoBackup]);

  // Calculate high-level status for header indicator
  const sheetsConnected = Boolean(sheetsTokenInfo?.token);
  const driveConnected = Boolean(driveTokenInfo?.token);
  const isSyncing = isSyncingSheets || isUploadingDrive;

  let workspaceStatus: 'syncing' | 'synced' | 'ready' | 'warning' | 'disconnected' = 'disconnected';
  let statusBadgeText = 'G-Sync';
  let statusBadgeTooltip = 'Google Workspace: Click to connect Google Sheets & Drive';

  if (isSyncing) {
    workspaceStatus = 'syncing';
    statusBadgeText = isSyncingSheets ? 'Sheets Syncing...' : 'Drive Uploading...';
    statusBadgeTooltip = 'Google Workspace synchronizing live data in background...';
  } else if (sheetsConnected && spreadsheetId) {
    workspaceStatus = 'synced';
    if (driveConnected) {
      statusBadgeText = 'Sheets & Drive';
      statusBadgeTooltip = `Google Sheets (Live Sync Active) & Google Drive (${driveBackupsCount} backups) Connected`;
    } else {
      statusBadgeText = 'Sheets Live';
      statusBadgeTooltip = `Google Sheets: Linked to "${spreadsheetTitle || spreadsheetId}" (Auto-sync ${sheetsAutoSync ? 'ON' : 'OFF'})`;
    }
  } else if (driveConnected) {
    workspaceStatus = 'ready';
    statusBadgeText = 'Drive Ready';
    statusBadgeTooltip = `Google Drive: Connected (${driveBackupsCount} backups saved)`;
  } else if (sheetsConnected) {
    workspaceStatus = 'ready';
    statusBadgeText = 'Sheets Ready';
    statusBadgeTooltip = 'Google Sheets: Authorized. Click to select or create a spreadsheet.';
  }

  return {
    // Sheets
    sheetsConnected,
    sheetsToken: sheetsTokenInfo?.token || null,
    spreadsheetId,
    spreadsheetTitle,
    spreadsheetUrl,
    lastSheetsSync,
    sheetsAutoSync,
    isSyncingSheets,
    sheetsError,
    sheetsSuccessMsg,

    // Drive
    driveConnected,
    driveToken: driveTokenInfo?.token || null,
    driveFolderId,
    lastDriveBackup,
    driveAutoBackup,
    isUploadingDrive,
    driveError,
    driveSuccessMsg,
    driveBackupsCount,

    // Workspace overview
    workspaceStatus,
    statusBadgeText,
    statusBadgeTooltip,

    // Actions
    syncAllSheets,
    syncTransactionToSheets,
    backupToDrive,
    connectSheets,
    connectDrive,
    disconnectSheets,
    disconnectDrive,
    toggleSheetsAutoSync,
    toggleDriveAutoBackup,
    refreshWorkspaceState
  };
}
