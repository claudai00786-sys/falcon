import React from 'react';
import {
  X,
  FileSpreadsheet,
  Cloud,
  CheckCircle2,
  AlertCircle,
  RefreshCw,
  ExternalLink,
  UploadCloud,
  Database,
  ShieldCheck,
  Zap,
  ArrowRight,
  HardDrive
} from 'lucide-react';
import { AppState } from '../types';

interface WorkspaceSyncModalProps {
  isOpen: boolean;
  onClose: () => void;
  appState: AppState;
  // Workspace sync hook states & actions
  sheetsConnected: boolean;
  spreadsheetId: string | null;
  spreadsheetTitle: string | null;
  spreadsheetUrl: string | null;
  lastSheetsSync: string | null;
  sheetsAutoSync: boolean;
  isSyncingSheets: boolean;
  sheetsError: string | null;
  sheetsSuccessMsg: string | null;
  onSyncAllSheets: () => Promise<boolean>;
  onConnectSheets: () => Promise<string | null>;
  onDisconnectSheets: () => void;
  onToggleSheetsAutoSync: (enabled?: boolean) => void;

  driveConnected: boolean;
  driveFolderId: string | null;
  lastDriveBackup: string | null;
  driveAutoBackup: boolean;
  isUploadingDrive: boolean;
  driveError: string | null;
  driveSuccessMsg: string | null;
  driveBackupsCount: number;
  onBackupToDrive: (format: 'json' | 'sql') => Promise<boolean>;
  onConnectDrive: () => Promise<string | null>;
  onDisconnectDrive: () => void;
  onToggleDriveAutoBackup: (enabled?: boolean) => void;

  // Navigation
  onNavigateToBackupTab: (tab: 'google_sheets' | 'google_drive' | 'cloud_status') => void;
  // Firestore sync state
  syncState: 'synced' | 'syncing' | 'offline' | 'error';
  pendingQueueCount?: number;
}

export const WorkspaceSyncModal: React.FC<WorkspaceSyncModalProps> = ({
  isOpen,
  onClose,
  appState,
  sheetsConnected,
  spreadsheetId,
  spreadsheetTitle,
  spreadsheetUrl,
  lastSheetsSync,
  sheetsAutoSync,
  isSyncingSheets,
  sheetsError,
  sheetsSuccessMsg,
  onSyncAllSheets,
  onConnectSheets,
  onDisconnectSheets,
  onToggleSheetsAutoSync,
  driveConnected,
  driveFolderId,
  lastDriveBackup,
  driveAutoBackup,
  isUploadingDrive,
  driveError,
  driveSuccessMsg,
  driveBackupsCount,
  onBackupToDrive,
  onConnectDrive,
  onDisconnectDrive,
  onToggleDriveAutoBackup,
  onNavigateToBackupTab,
  syncState,
  pendingQueueCount = 0
}) => {
  if (!isOpen) return null;

  const totalTransactions = appState.transactions?.length || 0;
  const totalProducts = appState.products?.length || 0;
  const totalCustomers = appState.customerLedgers?.length || 0;

  const effectiveSheetUrl =
    spreadsheetUrl ||
    (spreadsheetId ? `https://docs.google.com/spreadsheets/d/${spreadsheetId}/edit` : null);

  const effectiveDriveUrl = driveFolderId
    ? `https://drive.google.com/drive/folders/${driveFolderId}`
    : 'https://drive.google.com';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/75 backdrop-blur-xs animate-in fade-in duration-200">
      <div
        className="relative w-full max-w-3xl max-h-[90vh] flex flex-col rounded-2xl border border-[var(--steel-line)] bg-[var(--panel)] shadow-2xl overflow-hidden"
        onClick={e => e.stopPropagation()}
      >
        {/* HEADER */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--steel-line)] bg-[var(--panel-raised)]">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-500/15 border border-amber-500/30 flex items-center justify-center text-amber-400">
              <Cloud size={20} />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-[var(--text)] tracking-tight">
                  Google Workspace & Cloud Sync
                </h2>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 font-semibold uppercase">
                  Live Hub
                </span>
              </div>
              <p className="text-xs text-[var(--text-dim)]">
                Master synchronization for Google Sheets, Google Drive Backups, and Cloud POS
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-lg flex items-center justify-center text-[var(--text-dim)] hover:text-[var(--text)] hover:bg-[var(--panel-hover)] transition cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {/* NOTIFICATIONS & MESSAGES */}
        {(sheetsSuccessMsg || driveSuccessMsg) && (
          <div className="px-5 py-2.5 bg-emerald-500/10 border-b border-emerald-500/20 text-emerald-400 text-xs flex items-center gap-2">
            <CheckCircle2 size={15} className="shrink-0" />
            <span>{sheetsSuccessMsg || driveSuccessMsg}</span>
          </div>
        )}
        {(sheetsError || driveError) && (
          <div className="px-5 py-2.5 bg-red-500/10 border-b border-red-500/20 text-red-400 text-xs flex items-center gap-2">
            <AlertCircle size={15} className="shrink-0" />
            <span>{sheetsError || driveError}</span>
          </div>
        )}

        {/* MODAL CONTENT */}
        <div className="flex-1 overflow-y-auto p-5 space-y-4">
          {/* STATS STRIP */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
            <div className="p-3 rounded-xl border border-[var(--steel-line)] bg-[var(--panel-raised)]">
              <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">POS Firestore</span>
              <div className="flex items-center gap-1.5 mt-1">
                <span
                  className={`w-2 h-2 rounded-full ${
                    syncState === 'synced' ? 'bg-emerald-400' : syncState === 'syncing' ? 'bg-amber-400 animate-pulse' : 'bg-red-400'
                  }`}
                />
                <span className="text-xs font-bold text-[var(--text)] capitalize">{syncState}</span>
                {pendingQueueCount > 0 && (
                  <span className="text-[10px] font-mono text-amber-400">({pendingQueueCount} queued)</span>
                )}
              </div>
            </div>

            <div className="p-3 rounded-xl border border-[var(--steel-line)] bg-[var(--panel-raised)]">
              <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">Google Sheets</span>
              <div className="flex items-center gap-1.5 mt-1">
                <span
                  className={`w-2 h-2 rounded-full ${
                    isSyncingSheets
                      ? 'bg-amber-400 animate-pulse'
                      : sheetsConnected && spreadsheetId
                      ? 'bg-emerald-400 shadow-[0_0_8px_rgba(16,185,129,0.5)]'
                      : sheetsConnected
                      ? 'bg-sky-400'
                      : 'bg-slate-500'
                  }`}
                />
                <span className="text-xs font-bold text-[var(--text)]">
                  {isSyncingSheets ? 'Syncing...' : sheetsConnected && spreadsheetId ? 'Live Synced' : sheetsConnected ? 'Ready' : 'Not Connected'}
                </span>
              </div>
            </div>

            <div className="p-3 rounded-xl border border-[var(--steel-line)] bg-[var(--panel-raised)]">
              <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">Google Drive</span>
              <div className="flex items-center gap-1.5 mt-1">
                <span
                  className={`w-2 h-2 rounded-full ${
                    isUploadingDrive
                      ? 'bg-amber-400 animate-pulse'
                      : driveConnected
                      ? 'bg-sky-400 shadow-[0_0_8px_rgba(56,189,248,0.5)]'
                      : 'bg-slate-500'
                  }`}
                />
                <span className="text-xs font-bold text-[var(--text)]">
                  {isUploadingDrive ? 'Uploading...' : driveConnected ? `${driveBackupsCount} Backups` : 'Not Connected'}
                </span>
              </div>
            </div>

            <div className="p-3 rounded-xl border border-[var(--steel-line)] bg-[var(--panel-raised)]">
              <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">Live Database</span>
              <span className="text-xs font-mono font-bold text-[var(--text)] mt-1 block">
                {totalTransactions} Txns • {totalProducts} SKUs
              </span>
            </div>
          </div>

          {/* CARD 1: GOOGLE SHEETS LIVE SYNC */}
          <div className="p-4 rounded-xl border border-[var(--steel-line)] bg-[var(--panel-raised)] space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2 pb-3 border-b border-[var(--steel-line)]">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
                  <FileSpreadsheet size={16} />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[var(--text)] flex items-center gap-2">
                    Google Sheets Real-time Synchronization
                    {sheetsConnected && (
                      <span className="px-1.5 py-0.5 rounded text-[9px] font-mono bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                        Authorized
                      </span>
                    )}
                  </h3>
                  <p className="text-[11px] text-[var(--text-dim)]">
                    Maintains 7 live worksheets: Dashboard, Sales, Catalog, Ledgers, Raw Stock, Factories & Expenses
                  </p>
                </div>
              </div>

              <div>
                {!sheetsConnected ? (
                  <button
                    type="button"
                    onClick={onConnectSheets}
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold shadow-xs transition cursor-pointer"
                  >
                    <ShieldCheck size={14} />
                    <span>Connect Google Account</span>
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={onDisconnectSheets}
                    className="text-[11px] text-red-400 hover:underline cursor-pointer"
                  >
                    Disconnect
                  </button>
                )}
              </div>
            </div>

            {/* Sheets Details / Controls */}
            {sheetsConnected ? (
              <div className="space-y-3 pt-1">
                <div className="flex flex-wrap items-center justify-between gap-2 p-2.5 rounded-lg bg-[var(--panel)] border border-[var(--steel-line)]">
                  <div className="min-w-0">
                    <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">Linked Spreadsheet</span>
                    {spreadsheetId ? (
                      <div className="flex items-center gap-2 mt-0.5">
                        <span className="text-xs font-bold text-[var(--text)] truncate max-w-xs sm:max-w-md">
                          {spreadsheetTitle || spreadsheetId}
                        </span>
                        {effectiveSheetUrl && (
                          <a
                            href={effectiveSheetUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center gap-1 text-[11px] text-emerald-400 hover:underline"
                          >
                            <span>Open</span>
                            <ExternalLink size={11} />
                          </a>
                        )}
                      </div>
                    ) : (
                      <span className="text-xs text-amber-400">
                        No target spreadsheet chosen yet. Go to Sheets tab to select or generate one.
                      </span>
                    )}
                  </div>

                  {lastSheetsSync && (
                    <div className="text-right">
                      <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">Last Synchronized</span>
                      <span className="text-xs font-mono text-[var(--text)]">{lastSheetsSync}</span>
                    </div>
                  )}
                </div>

                <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
                  <label className="flex items-center gap-2 cursor-pointer text-xs">
                    <input
                      type="checkbox"
                      checked={sheetsAutoSync}
                      onChange={e => onToggleSheetsAutoSync(e.target.checked)}
                      className="rounded border-[var(--steel-line)] text-emerald-500 focus:ring-emerald-500 bg-[var(--panel)]"
                    />
                    <span className={sheetsAutoSync ? 'text-emerald-400 font-semibold' : 'text-[var(--text-dim)]'}>
                      Auto-sync on new sales transactions
                    </span>
                  </label>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      disabled={isSyncingSheets || !spreadsheetId}
                      onClick={() => onSyncAllSheets()}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-xs font-semibold shadow-xs transition cursor-pointer"
                    >
                      <RefreshCw size={13} className={isSyncingSheets ? 'animate-spin' : ''} />
                      <span>{isSyncingSheets ? 'Syncing 7 Sheets...' : 'Sync All Sheets Now'}</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => {
                        onClose();
                        onNavigateToBackupTab('google_sheets');
                      }}
                      className="flex items-center gap-1 text-xs text-[var(--text-dim)] hover:text-[var(--text)] px-2 py-1.5 rounded-lg hover:bg-[var(--panel-hover)] transition cursor-pointer"
                    >
                      <span>Manage Tabs</span>
                      <ArrowRight size={13} />
                    </button>
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-xs text-[var(--text-dim)] p-2.5 rounded-lg bg-[var(--panel)] border border-dashed border-[var(--steel-line)]">
                Connect your master Google Account to unlock real-time streaming of all workshop receipts, inventory changes, customer balances, and raw material stocks directly to Google Sheets.
              </div>
            )}
          </div>

          {/* CARD 2: GOOGLE DRIVE CLOUD BACKUPS */}
          <div className="p-4 rounded-xl border border-[var(--steel-line)] bg-[var(--panel-raised)] space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2 pb-3 border-b border-[var(--steel-line)]">
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-lg bg-sky-500/15 border border-sky-500/30 flex items-center justify-center text-sky-400">
                  <Cloud size={16} />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[var(--text)] flex items-center gap-2">
                    Google Drive Dedicated Backups
                    {driveConnected && (
                      <span className="px-1.5 py-0.5 rounded text-[9px] font-mono bg-sky-500/15 text-sky-400 border border-sky-500/30">
                        Authorized
                      </span>
                    )}
                  </h3>
                  <p className="text-[11px] text-[var(--text-dim)]">
                    Isolated snapshot archives stored in "Falcon Rod Maker POS - Database Backups"
                  </p>
                </div>
              </div>

              <div>
                {!driveConnected ? (
                  <button
                    type="button"
                    onClick={onConnectDrive}
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 text-white text-xs font-semibold shadow-xs transition cursor-pointer"
                  >
                    <UploadCloud size={14} />
                    <span>Authorize Drive</span>
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={onDisconnectDrive}
                    className="text-[11px] text-red-400 hover:underline cursor-pointer"
                  >
                    Disconnect
                  </button>
                )}
              </div>
            </div>

            {/* Drive Details / Controls */}
            {driveConnected ? (
              <div className="space-y-3 pt-1">
                <div className="flex flex-wrap items-center justify-between gap-2 p-2.5 rounded-lg bg-[var(--panel)] border border-[var(--steel-line)]">
                  <div className="min-w-0">
                    <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">Backup Folder</span>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs font-bold text-[var(--text)]">
                        Falcon Rod Maker POS - Database Backups
                      </span>
                      <a
                        href={effectiveDriveUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1 text-[11px] text-sky-400 hover:underline"
                      >
                        <span>Open Drive</span>
                        <ExternalLink size={11} />
                      </a>
                    </div>
                  </div>

                  <div className="flex items-center gap-4">
                    <div>
                      <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">Saved Backups</span>
                      <span className="text-xs font-mono font-bold text-sky-400">{driveBackupsCount} Files</span>
                    </div>
                    {lastDriveBackup && (
                      <div className="text-right">
                        <span className="text-[10px] font-mono uppercase text-[var(--text-dim)] block">Last Upload</span>
                        <span className="text-xs font-mono text-[var(--text)]">
                          {new Date(lastDriveBackup).toLocaleDateString('en-GB')}
                        </span>
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
                  <label className="flex items-center gap-2 cursor-pointer text-xs">
                    <input
                      type="checkbox"
                      checked={driveAutoBackup}
                      onChange={e => onToggleDriveAutoBackup(e.target.checked)}
                      className="rounded border-[var(--steel-line)] text-sky-500 focus:ring-sky-500 bg-[var(--panel)]"
                    />
                    <span className={driveAutoBackup ? 'text-sky-400 font-semibold' : 'text-[var(--text-dim)]'}>
                      Auto-backup on terminal session start
                    </span>
                  </label>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      disabled={isUploadingDrive}
                      onClick={() => onBackupToDrive('json')}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 disabled:opacity-50 text-white text-xs font-semibold shadow-xs transition cursor-pointer"
                    >
                      <UploadCloud size={13} className={isUploadingDrive ? 'animate-spin' : ''} />
                      <span>{isUploadingDrive ? 'Uploading...' : 'Backup JSON'}</span>
                    </button>

                    <button
                      type="button"
                      disabled={isUploadingDrive}
                      onClick={() => onBackupToDrive('sql')}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[var(--panel-hover)] hover:bg-[var(--steel-line)] disabled:opacity-50 text-[var(--text)] text-xs font-semibold border border-[var(--steel-line)] transition cursor-pointer"
                    >
                      <Database size={13} />
                      <span>Backup SQL</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => {
                        onClose();
                        onNavigateToBackupTab('google_drive');
                      }}
                      className="flex items-center gap-1 text-xs text-[var(--text-dim)] hover:text-[var(--text)] px-2 py-1.5 rounded-lg hover:bg-[var(--panel-hover)] transition cursor-pointer"
                    >
                      <span>Explore & Restore</span>
                      <ArrowRight size={13} />
                    </button>
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-xs text-[var(--text-dim)] p-2.5 rounded-lg bg-[var(--panel)] border border-dashed border-[var(--steel-line)]">
                Connect Google Drive to safely store full point-in-time JSON database snapshots and PostgreSQL schema dumps for instant cloud disaster recovery.
              </div>
            )}
          </div>
        </div>

        {/* FOOTER */}
        <div className="flex items-center justify-between px-5 py-3 border-t border-[var(--steel-line)] bg-[var(--panel-raised)]">
          <div className="flex items-center gap-2 text-[11px] text-[var(--text-dim)] font-mono">
            <Zap size={13} className="text-amber-400" />
            <span>Authorized Owner: umarzaman7777777@gmail.com</span>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => {
                onClose();
                onNavigateToBackupTab('cloud_status');
              }}
              className="px-3 py-1.5 rounded-lg text-xs font-semibold text-[var(--text-dim)] hover:text-[var(--text)] hover:bg-[var(--panel-hover)] transition cursor-pointer"
            >
              Full Diagnostics
            </button>
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-1.5 rounded-lg bg-[var(--yellow)] hover:bg-[var(--yellow)]/90 text-black text-xs font-bold transition cursor-pointer"
            >
              Done
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
