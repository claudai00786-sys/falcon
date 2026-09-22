import firebaseConfig from '../../firebase-applet-config.json';
import { auth } from '../firebase/config';
import { signInWithPopup, GoogleAuthProvider } from 'firebase/auth';
import { AppState, Transaction, Product, CustomerLedgerAccount, RawStockItem, Expense, Factory } from '../types';

declare global {
  interface Window {
    google?: any;
  }
}

export const ALLOWED_SHEETS_OWNER_EMAIL = 'umarzaman7777777@gmail.com';

export const SHEETS_SCOPES = [
  'https://www.googleapis.com/auth/spreadsheets',
  'https://www.googleapis.com/auth/drive.file'
].join(' ');

const STORAGE_KEY_SHEETS_TOKEN = 'falcon_gsheets_token';
const STORAGE_KEY_SHEETS_EXPIRES = 'falcon_gsheets_expires_at';
const STORAGE_KEY_SHEETS_EMAIL = 'falcon_gsheets_email';
const STORAGE_KEY_SHEETS_SPREADSHEET_ID = 'falcon_gsheets_spreadsheet_id';
const STORAGE_KEY_SHEETS_SPREADSHEET_TITLE = 'falcon_gsheets_spreadsheet_title';
const STORAGE_KEY_SHEETS_SPREADSHEET_URL = 'falcon_gsheets_spreadsheet_url';
const STORAGE_KEY_SHEETS_LAST_SYNC = 'falcon_gsheets_last_sync';
const STORAGE_KEY_SHEETS_AUTO_SYNC = 'falcon_gsheets_auto_sync';

export const WORKSPACE_SYNC_EVENT = 'falcon_workspace_sync_event';

export function notifyWorkspaceSyncUpdated(): void {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent(WORKSPACE_SYNC_EVENT));
  }
}

export interface SheetsTokenInfo {
  token: string;
  expiresAt: number;
  userEmail?: string;
}

export interface SheetMetadata {
  id: number;
  title: string;
  rowCount: number;
  columnCount: number;
}

export interface SpreadsheetInfo {
  spreadsheetId: string;
  title: string;
  spreadsheetUrl: string;
  sheets: SheetMetadata[];
}

export interface SheetSyncResult {
  sheetName: string;
  rowsUpdated: number;
  status: 'success' | 'error';
  error?: string;
}

export interface FullSyncReport {
  spreadsheetId: string;
  spreadsheetUrl: string;
  syncedAt: string;
  results: SheetSyncResult[];
  totalRowsSynced: number;
}

/**
 * Retrieve cached Google Sheets OAuth access token if still valid
 */
export function getStoredSheetsToken(): SheetsTokenInfo | null {
  try {
    const token = localStorage.getItem(STORAGE_KEY_SHEETS_TOKEN);
    const expiresStr = localStorage.getItem(STORAGE_KEY_SHEETS_EXPIRES);
    const userEmail = localStorage.getItem(STORAGE_KEY_SHEETS_EMAIL) || undefined;

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
 * Persist access token in localStorage
 */
export function storeSheetsToken(token: string, expiresInSeconds: number, email?: string): void {
  try {
    const expiresAt = Date.now() + (expiresInSeconds * 1000);
    localStorage.setItem(STORAGE_KEY_SHEETS_TOKEN, token);
    localStorage.setItem(STORAGE_KEY_SHEETS_EXPIRES, expiresAt.toString());
    if (email) {
      localStorage.setItem(STORAGE_KEY_SHEETS_EMAIL, email);
    }
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store Sheets token in localStorage', err);
  }
}

/**
 * Clear cached Google Sheets credentials
 */
export function clearSheetsToken(): void {
  try {
    localStorage.removeItem(STORAGE_KEY_SHEETS_TOKEN);
    localStorage.removeItem(STORAGE_KEY_SHEETS_EXPIRES);
    localStorage.removeItem(STORAGE_KEY_SHEETS_EMAIL);
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to clear Sheets token', err);
  }
}

export function getStoredSpreadsheetId(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY_SHEETS_SPREADSHEET_ID);
  } catch {
    return null;
  }
}

export function setStoredSpreadsheetId(id: string | null): void {
  try {
    if (id) {
      localStorage.setItem(STORAGE_KEY_SHEETS_SPREADSHEET_ID, id.trim());
    } else {
      localStorage.removeItem(STORAGE_KEY_SHEETS_SPREADSHEET_ID);
    }
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store spreadsheet ID', err);
  }
}

export function getStoredSpreadsheetTitle(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY_SHEETS_SPREADSHEET_TITLE);
  } catch {
    return null;
  }
}

export function setStoredSpreadsheetTitle(title: string | null): void {
  try {
    if (title) {
      localStorage.setItem(STORAGE_KEY_SHEETS_SPREADSHEET_TITLE, title.trim());
    } else {
      localStorage.removeItem(STORAGE_KEY_SHEETS_SPREADSHEET_TITLE);
    }
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store spreadsheet title', err);
  }
}

export function getStoredSpreadsheetUrl(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY_SHEETS_SPREADSHEET_URL);
  } catch {
    return null;
  }
}

export function setStoredSpreadsheetUrl(url: string | null): void {
  try {
    if (url) {
      localStorage.setItem(STORAGE_KEY_SHEETS_SPREADSHEET_URL, url.trim());
    } else {
      localStorage.removeItem(STORAGE_KEY_SHEETS_SPREADSHEET_URL);
    }
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store spreadsheet url', err);
  }
}

export function getLastSheetsSyncTime(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY_SHEETS_LAST_SYNC);
  } catch {
    return null;
  }
}

export function setLastSheetsSyncTime(time: string): void {
  try {
    localStorage.setItem(STORAGE_KEY_SHEETS_LAST_SYNC, time);
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store last sync time', err);
  }
}

export function getSheetsAutoSyncEnabled(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY_SHEETS_AUTO_SYNC) === 'true';
  } catch {
    return false;
  }
}

export function setSheetsAutoSyncEnabled(enabled: boolean): void {
  try {
    localStorage.setItem(STORAGE_KEY_SHEETS_AUTO_SYNC, enabled ? 'true' : 'false');
    notifyWorkspaceSyncUpdated();
  } catch (err) {
    console.error('Failed to store auto-sync preference', err);
  }
}

/**
 * Acquire Google Sheets & Drive OAuth token using GSI initTokenClient or Firebase Auth popup
 */
export async function requestGoogleSheetsToken(preferredEmail: string = ALLOWED_SHEETS_OWNER_EMAIL): Promise<string> {
  const clientId = firebaseConfig.oAuthClientId;

  // 1. Try GSI Token Client if available in window.google
  if (typeof window !== 'undefined' && window.google?.accounts?.oauth2 && clientId) {
    try {
      const token = await new Promise<string>((resolve, reject) => {
        const tokenClient = window.google.accounts.oauth2.initTokenClient({
          client_id: clientId,
          scope: SHEETS_SCOPES,
          hint: preferredEmail || ALLOWED_SHEETS_OWNER_EMAIL,
          prompt: '',
          callback: (response: any) => {
            if (response.error) {
              reject(new Error(response.error_description || response.error));
            } else if (response.access_token) {
              const expiresIn = response.expires_in ? parseInt(response.expires_in, 10) : 3500;
              storeSheetsToken(response.access_token, expiresIn, preferredEmail || ALLOWED_SHEETS_OWNER_EMAIL);
              resolve(response.access_token);
            } else {
              reject(new Error('No access token returned by Google Identity Services.'));
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

  // 2. Fallback: Firebase Auth with GoogleAuthProvider adding sheets & drive scopes
  try {
    const provider = new GoogleAuthProvider();
    provider.addScope('https://www.googleapis.com/auth/spreadsheets');
    provider.addScope('https://www.googleapis.com/auth/drive.file');
    provider.setCustomParameters({
      prompt: 'select_account',
      login_hint: preferredEmail || ALLOWED_SHEETS_OWNER_EMAIL
    });

    const result = await signInWithPopup(auth, provider);
    const authedEmail = result.user.email || preferredEmail || ALLOWED_SHEETS_OWNER_EMAIL;

    const credential = GoogleAuthProvider.credentialFromResult(result);
    const accessToken = credential?.accessToken;

    if (!accessToken) {
      throw new Error('Could not acquire Google Sheets access token from authentication result.');
    }

    storeSheetsToken(accessToken, 3500, authedEmail);
    return accessToken;
  } catch (error: any) {
    throw new Error(error?.message || 'Authentication with Google Sheets was cancelled or failed.');
  }
}

/**
 * Fetch spreadsheet metadata and worksheets list
 */
export async function getSpreadsheetInfo(accessToken: string, spreadsheetId: string): Promise<SpreadsheetInfo> {
  const cleanId = extractSpreadsheetId(spreadsheetId);
  const response = await fetch(`https://sheets.googleapis.com/v4/spreadsheets/${cleanId}?fields=spreadsheetId,properties.title,spreadsheetUrl,sheets.properties`, {
    headers: {
      Authorization: `Bearer ${accessToken}`
    }
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody?.error?.message || `Failed to fetch Google Spreadsheet (${response.status})`);
  }

  const data = await response.json();
  const sheets: SheetMetadata[] = (data.sheets || []).map((s: any) => ({
    id: s.properties.sheetId,
    title: s.properties.title,
    rowCount: s.properties.gridProperties?.rowCount || 0,
    columnCount: s.properties.gridProperties?.columnCount || 0
  }));

  const info: SpreadsheetInfo = {
    spreadsheetId: data.spreadsheetId,
    title: data.properties?.title || 'Falcon Rod Maker POS Master Sheet',
    spreadsheetUrl: data.spreadsheetUrl || `https://docs.google.com/spreadsheets/d/${data.spreadsheetId}/edit`,
    sheets
  };

  setStoredSpreadsheetTitle(info.title);
  setStoredSpreadsheetUrl(info.spreadsheetUrl);

  return info;
}

/**
 * Extract clean spreadsheet ID from either a raw ID or full Google Sheets URL
 */
export function extractSpreadsheetId(input: string): string {
  if (!input) return '';
  const trimmed = input.trim();
  const urlMatch = trimmed.match(/\/spreadsheets\/d\/([a-zA-Z0-9-_]+)/);
  if (urlMatch && urlMatch[1]) {
    return urlMatch[1];
  }
  return trimmed;
}

/**
 * Defined structure of default Falcon Rod Maker Worksheets
 */
export const DEFAULT_SHEET_TABS = [
  'Dashboard',
  'Sales Transactions',
  'Product Catalog',
  'Customer Ledgers',
  'Raw Material Stock',
  'Factory Production',
  'Workshop Expenses'
];

export const ALL_EXTENDED_SHEET_TABS = [
  'Dashboard',
  'Sales Transactions',
  'Product Catalog',
  'Customer Ledgers',
  'Customer Payments',
  'Raw Material Stock',
  'Raw Suppliers',
  'Factory Production',
  'Labour Workers',
  'Paint Ledger',
  'Scrap Ledger',
  'Withdrawals',
  'Product Returns',
  'Workshop Expenses',
  'Custom Ledgers',
  'Inquiries'
];

export function getAllApplicableSheetTabs(appState?: AppState): string[] {
  const tabs = [...ALL_EXTENDED_SHEET_TABS];
  if (appState?.customLedgersList && appState.customLedgersList.length > 0) {
    for (const cl of appState.customLedgersList) {
      if (cl.name && !tabs.includes(cl.name)) {
        tabs.push(cl.name);
      }
    }
  }
  return tabs;
}

/**
 * Create a new master Falcon Rod Maker Google Spreadsheet with all standard tabs pre-configured
 */
export async function createMasterFalconSpreadsheet(
  accessToken: string,
  appState: AppState,
  customTitle?: string
): Promise<SpreadsheetInfo> {
  const title = customTitle || `Falcon Rod Maker POS - Master Workshop Database (${new Date().toLocaleDateString('en-GB')})`;

  const requestBody = {
    properties: {
      title,
      locale: 'en_PK',
      timeZone: 'Asia/Karachi'
    },
    sheets: DEFAULT_SHEET_TABS.map((tabTitle, idx) => ({
      properties: {
        sheetId: idx + 1,
        title: tabTitle,
        gridProperties: {
          rowCount: 200,
          columnCount: 20,
          frozenRowCount: 1
        }
      }
    }))
  };

  const response = await fetch('https://sheets.googleapis.com/v4/spreadsheets', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestBody)
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody?.error?.message || `Failed to create new Google Spreadsheet (${response.status})`);
  }

  const data = await response.json();
  const spreadsheetId = data.spreadsheetId;

  // Format headers and populate initial data
  await populateAllSheets(accessToken, spreadsheetId, appState);
  await formatSpreadsheetHeaders(accessToken, spreadsheetId);

  setStoredSpreadsheetId(spreadsheetId);
  setLastSheetsSyncTime(new Date().toISOString());

  return getSpreadsheetInfo(accessToken, spreadsheetId);
}

/**
 * Apply styling (amber header, bold text, borders) to all sheets in the spreadsheet
 */
export async function formatSpreadsheetHeaders(accessToken: string, spreadsheetId: string): Promise<void> {
  try {
    const info = await getSpreadsheetInfo(accessToken, spreadsheetId);
    const requests = info.sheets.map(sheet => ({
      repeatCell: {
        range: {
          sheetId: sheet.id,
          startRowIndex: 0,
          endRowIndex: 1
        },
        cell: {
          userEnteredFormat: {
            backgroundColor: { red: 0.96, green: 0.62, blue: 0.04 }, // Amber #F59E0B
            textFormat: {
              bold: true,
              fontSize: 10,
              foregroundColor: { red: 0.05, green: 0.05, blue: 0.05 }
            },
            horizontalAlignment: 'CENTER',
            verticalAlignment: 'MIDDLE'
          }
        },
        fields: 'userEnteredFormat(backgroundColor,textFormat,horizontalAlignment,verticalAlignment)'
      }
    }));

    await fetch(`https://sheets.googleapis.com/v4/spreadsheets/${spreadsheetId}:batchUpdate`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ requests })
    });
  } catch (err) {
    console.warn('Non-blocking header formatting notice:', err);
  }
}

/**
 * Ensure all standard tabs exist in an existing spreadsheet
 */
export async function ensureRequiredSheetsExist(accessToken: string, spreadsheetId: string): Promise<void> {
  const info = await getSpreadsheetInfo(accessToken, spreadsheetId);
  const existingTitles = new Set(info.sheets.map(s => s.title));

  const missingTabs = DEFAULT_SHEET_TABS.filter(t => !existingTitles.has(t));
  if (missingTabs.length === 0) return;

  const requests = missingTabs.map(title => ({
    addSheet: {
      properties: {
        title,
        gridProperties: {
          rowCount: 200,
          columnCount: 20,
          frozenRowCount: 1
        }
      }
    }
  }));

  const res = await fetch(`https://sheets.googleapis.com/v4/spreadsheets/${spreadsheetId}:batchUpdate`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ requests })
  });

  if (!res.ok) {
    const errorBody = await res.json().catch(() => ({}));
    console.warn('Could not add missing sheets to existing workbook:', errorBody);
  }
}

// =========================================================================
// DATA GENERATORS & VALUES BUILDERS
// =========================================================================

export function buildDashboardValues(appState: AppState): (string | number)[][] {
  const transactions = appState.transactions || [];
  const totalSales = transactions.reduce((sum, t) => sum + (Number(t.total) || 0), 0);
  const totalPaid = transactions.reduce((sum, t) => sum + (t.paid ? Number(t.total) || 0 : 0), 0);
  const pendingBalance = totalSales - totalPaid;
  const productsCount = (appState.products || []).length;
  const totalStockUnits = (appState.products || []).reduce((sum, p) => sum + (Number(p.stock) || 0), 0);
  const customersCount = (appState.customerLedgers || []).length;
  const rawStockItems = (appState.rawStock || []).length;
  const expensesTotal = (appState.expenses || []).reduce((sum, e) => sum + (Number(e.amount) || 0), 0);

  return [
    ['FALCON ROD MAKER POS - WORKSHOP METRIC', 'VALUE', 'UNIT / NOTES'],
    ['Workshop Name', appState.companyName || 'Falcon Rod Maker', 'Gujrat Industrial Zone'],
    ['Authorized Master Account', ALLOWED_SHEETS_OWNER_EMAIL, 'Owner Security Pass'],
    ['Last Cloud Synchronization', new Date().toLocaleString('en-GB'), 'Automated Sync'],
    ['Total Lifetime Gross Sales (PKR)', Math.round(totalSales), 'PKR'],
    ['Total Recorded Customer Payments', Math.round(totalPaid), 'PKR'],
    ['Outstanding Uncollected Balance', Math.round(pendingBalance), 'PKR (Receivable)'],
    ['Total Sales Invoices Issued', transactions.length, 'Invoices'],
    ['Active Catalog Products', productsCount, 'SKUs'],
    ['Total Finished Rods In Stock', totalStockUnits, 'Pieces Available'],
    ['Registered Customer Accounts', customersCount, 'Distributors & Buyers'],
    ['Raw Material Line Items', rawStockItems, 'Raw Materials'],
    ['Total Recorded Workshop Expenses', Math.round(expensesTotal), 'PKR']
  ];
}

export function buildSalesTransactionsValues(transactions: Transaction[]): (string | number)[][] {
  const headers = [
    'Invoice ID',
    'Date',
    'Time',
    'Customer / Factory',
    'Total Amount (PKR)',
    'Payment Status',
    'Payment Method',
    'Items Count',
    'Summary Description',
    'Sizes & Gauges',
    'Gate Sequence #',
    'Gate Receiver',
    'Terminal Device'
  ];

  const rows = (transactions || []).map(t => [
    t.id || '',
    t.date || '',
    t.time || '',
    t.factory || 'Walk-in Customer',
    Number(t.total) || 0,
    t.paid ? 'PAID' : 'PENDING',
    t.method || (t.detailCash ? 'Cash' : t.detailBank ? 'Bank Transfer' : 'Unspecified'),
    t.itemCount || 1,
    (t.itemsSummary || '')
      .replace(/fan guards/gi, 'fan rods')
      .replace(/fan guard/gi, 'fan rod')
      .replace(/[\r\n]+/g, ' '),
    t.sizes || '',
    t.gateSequenceNo || (t.gateSequence ? `#${t.gateSequence}` : ''),
    t.gateReceivedBy || '',
    t.device || 'Counter Terminal'
  ]);

  return [headers, ...rows];
}

export function buildProductCatalogValues(products: Product[]): (string | number)[][] {
  const headers = [
    'Item ID',
    'Product Name',
    'Category',
    'Unit Price (PKR)',
    'Current Stock (Pcs)',
    'Total Stock Valuation (PKR)',
    'Available Sizes',
    'Gauge / Thickness',
    'Weight (Grams)',
    'Colour Variant',
    'Reorder Threshold'
  ];

  const rows = (products || []).map(p => {
    const stock = Number(p.stock) || 0;
    const price = Number(p.price) || 0;
    return [
      p.id,
      p.name || '',
      p.cat || '',
      price,
      stock,
      price * stock,
      Array.isArray(p.sizes) ? p.sizes.join(', ') : p.size || '',
      p.gauge || '',
      p.weight || '',
      p.color || '',
      p.reorderLevel || 10
    ];
  });

  return [headers, ...rows];
}

export function buildCustomerLedgerValues(customers: CustomerLedgerAccount[]): (string | number)[][] {
  const headers = [
    'Customer / Factory Name',
    'Total Debited (PKR)',
    'Total Credited (PKR)',
    'Outstanding Balance (PKR)',
    'Status',
    'Total Entries Recorded',
    'Last Transaction Date',
    'Last Transaction Note'
  ];

  const rows = (customers || []).map(c => {
    const entries = c.entries || [];
    const totalDebit = entries.reduce((s, e) => s + (Number(e.debit) || 0), 0);
    const totalCredit = entries.reduce((s, e) => s + (Number(e.credit) || 0), 0);
    const balance = totalDebit - totalCredit;
    const lastEntry = entries.length > 0 ? entries[entries.length - 1] : null;

    return [
      c.name || 'Unnamed Account',
      Math.round(totalDebit),
      Math.round(totalCredit),
      Math.round(balance),
      balance > 0 ? 'OWING BALANCE' : balance < 0 ? 'ADVANCE CREDIT' : 'SETTLED',
      entries.length,
      lastEntry?.date || '',
      (lastEntry?.desc || '').replace(/[\r\n]+/g, ' ')
    ];
  });

  return [headers, ...rows];
}

export function buildRawStockValues(rawStock: RawStockItem[]): (string | number)[][] {
  const headers = [
    'Raw Material Name',
    'Category',
    'Current Weight (KG)',
    'Current Items / Bundles',
    'Unit Type',
    'Initial Weight (KG)',
    'Low Stock Reorder Threshold',
    'Last Stock Update'
  ];

  const rows = (rawStock || []).map(r => [
    r.name || '',
    r.category || 'General Raw',
    Number(r.weight) || 0,
    Number(r.items) || Number(r.quantity) || 0,
    r.unit || 'kg',
    Number(r.initialWeight) || 0,
    r.lowStockThreshold || r.reorderLevel || 0,
    r.lastUpdated || ''
  ]);

  return [headers, ...rows];
}

export function buildFactoryProductionValues(factories: Factory[], transactions: Transaction[]): (string | number)[][] {
  const headers = [
    'Factory Name',
    'Factory Location',
    'Contact Number / Person',
    'Total Invoices Issued',
    'Total Production Billed (PKR)'
  ];

  const rows = (factories || []).map(f => {
    const factoryTxns = (transactions || []).filter(t => t.factory && t.factory.toLowerCase() === f.name.toLowerCase());
    const totalBilled = factoryTxns.reduce((sum, t) => sum + (Number(t.total) || 0), 0);

    return [
      f.name || '',
      f.location || '',
      f.contact || '',
      factoryTxns.length,
      Math.round(totalBilled)
    ];
  });

  return [headers, ...rows];
}

export function buildExpensesValues(expenses: Expense[]): (string | number)[][] {
  const headers = [
    'Expense ID',
    'Date',
    'Time',
    'Category',
    'Description',
    'Amount (PKR)',
    'Payment Method',
    'Recorded Terminal'
  ];

  const rows = (expenses || []).map(e => [
    e.id || '',
    e.date || '',
    e.time || '',
    e.category || 'General Overhead',
    (e.desc || '').replace(/[\r\n]+/g, ' '),
    Number(e.amount) || 0,
    e.method || 'Cash',
    e.device || 'Counter Terminal'
  ]);

  return [headers, ...rows];
}

export function buildTabValues(sheetTitle: string, appState: AppState): (string | number)[][] {
  const norm = (sheetTitle || '').trim().toLowerCase();

  // 1. Dashboard / Overview
  if (norm === 'dashboard' || norm === 'overview') {
    return buildDashboardValues(appState);
  }

  // 2. Sales Transactions
  if (norm === 'sales transactions' || norm === 'sales' || norm === 'transactions' || norm === 'orders' || norm === 'orders booked') {
    return buildSalesTransactionsValues(appState.transactions || []);
  }

  // 3. Product Catalog
  if (norm === 'product catalog' || norm === 'products' || norm === 'catalog' || norm === 'items') {
    return buildProductCatalogValues(appState.products || []);
  }

  // 4. Customer Ledgers / Factories
  if (norm === 'customer ledgers' || norm === 'customers' || norm === 'factories' || norm === 'factory ledgers') {
    return buildCustomerLedgerValues(appState.customerLedgers || []);
  }

  // 5. Customer Payments
  if (norm === 'customer payments' || norm === 'payments' || norm === 'receipts') {
    const headers = ['Payment ID', 'Txn ID', 'Date', 'Time', 'Amount (PKR)', 'Method', 'Detail / Notes', 'Received By', 'Device'];
    const rows = (appState.customerPayments || []).map(p => [
      p.id || '',
      p.txnId || '',
      p.date || '',
      p.time || '',
      Number(p.amount) || 0,
      p.method || 'Cash',
      (p.detail || '').replace(/[\r\n]+/g, ' '),
      p.receivedBy || '',
      p.device || ''
    ]);
    return [headers, ...rows];
  }

  // 6. Raw Material Stock
  if (norm === 'raw material stock' || norm === 'raw material' || norm === 'raw stock' || norm === 'raw inventory') {
    return buildRawStockValues(appState.rawStock || []);
  }

  // 7. Raw Suppliers
  if (norm === 'raw suppliers' || norm === 'suppliers' || norm === 'raw material suppliers') {
    const headers = ['Supplier Name', 'Total Entries', 'Total Debited (PKR)', 'Total Credited (PKR)', 'Net Balance (PKR)'];
    const rows = (appState.rawSuppliers || []).map(s => {
      const entries = s.entries || [];
      const totalDebit = entries.reduce((acc, e) => acc + (Number(e.debit) || 0), 0);
      const totalCredit = entries.reduce((acc, e) => acc + (Number(e.credit) || 0), 0);
      return [
        s.name || '',
        entries.length,
        Math.round(totalDebit),
        Math.round(totalCredit),
        Math.round(totalDebit - totalCredit)
      ];
    });
    return [headers, ...rows];
  }

  // 8. Factory Production
  if (norm === 'factory production' || norm === 'production') {
    return buildFactoryProductionValues(appState.factories || [], appState.transactions || []);
  }

  // 9. Labour Workers / Labour Ledger
  if (norm === 'labour workers' || norm === 'labour ledger' || norm === 'labour' || norm === 'workers') {
    const headers = ['Worker Name', 'Work Type', 'Rate Type', 'Standard Rate (PKR)', 'Total Entries', 'Net Balance (PKR)'];
    const workers = appState.labourWorkers || appState.workers || [];
    const rows = workers.map(w => {
      const entries = w.entries || [];
      const totalDebit = entries.reduce((acc, e) => acc + (Number(e.debit) || 0), 0);
      const totalCredit = entries.reduce((acc, e) => acc + (Number(e.credit) || 0), 0);
      return [
        w.name || '',
        w.workType || '',
        w.rateType || 'piece',
        Number(w.rate) || 0,
        entries.length,
        Math.round(totalDebit - totalCredit)
      ];
    });
    return [headers, ...rows];
  }

  // 10. Paint Ledger
  if (norm === 'paint ledger' || norm === 'paint' || norm === 'painters') {
    const headers = ['Painter Name', 'Colours Handled', 'Total Entries', 'Net Balance (PKR)'];
    const rows = (appState.painters || []).map(p => {
      const entries = p.entries || [];
      const totalDebit = entries.reduce((acc, e) => acc + (Number(e.debit) || 0), 0);
      const totalCredit = entries.reduce((acc, e) => acc + (Number(e.credit) || 0), 0);
      return [
        p.name || '',
        (p.colours || []).join(', '),
        entries.length,
        Math.round(totalDebit - totalCredit)
      ];
    });
    return [headers, ...rows];
  }

  // 11. Scrap Ledger
  if (norm === 'scrap ledger' || norm === 'scrap' || norm === 'scrap buyers') {
    const headers = ['Buyer Name', 'Total Entries', 'Net Balance (PKR)'];
    const rows = (appState.scrapBuyers || []).map(b => {
      const entries = b.entries || [];
      const totalDebit = entries.reduce((acc, e) => acc + (Number(e.debit) || 0), 0);
      const totalCredit = entries.reduce((acc, e) => acc + (Number(e.credit) || 0), 0);
      return [
        b.name || '',
        entries.length,
        Math.round(totalDebit - totalCredit)
      ];
    });
    return [headers, ...rows];
  }

  // 12. Withdrawals
  if (norm === 'withdrawals' || norm === 'withdrawal' || norm === 'drawings' || norm === 'owner drawings') {
    const headers = ['Withdrawal ID', 'Date', 'Amount (PKR)', 'Description', 'Method', 'Detail', 'Note', 'Withdrawn By'];
    const rows = (appState.withdrawals || []).map(w => [
      w.id || '',
      w.date || '',
      Number(w.amount) || 0,
      (w.desc || '').replace(/[\r\n]+/g, ' '),
      w.method || 'Cash',
      (w.detail || '').replace(/[\r\n]+/g, ' '),
      (w.note || '').replace(/[\r\n]+/g, ' '),
      w.withdrawnBy || ''
    ]);
    return [headers, ...rows];
  }

  // 13. Product Returns
  if (norm === 'product returns' || norm === 'returns' || norm === 'defects') {
    const headers = ['Return ID', 'Date', 'Factory / Customer', 'Product', 'Quantity', 'Reason', 'Status', 'Compensation (PKR)'];
    const returns = appState.productReturns || appState.returns || [];
    const rows = returns.map(r => [
      r.id || '',
      r.date || '',
      r.factory || '',
      r.productName || r.product || '',
      Number(r.quantity) || Number(r.qty) || 1,
      (r.reason || '').replace(/[\r\n]+/g, ' '),
      r.status || 'pending',
      Number(r.compensationRs) || Number(r.refundAmount) || 0
    ]);
    return [headers, ...rows];
  }

  // 14. Workshop Expenses
  if (norm === 'workshop expenses' || norm === 'expenses' || norm === 'daily expenses' || norm === 'overheads') {
    return buildExpensesValues(appState.expenses || []);
  }

  // 15. Custom Ledgers Summary
  if (norm === 'custom ledgers' || norm === 'custom ledgers summary') {
    const headers = ['Ledger ID', 'Ledger Name', 'Total Entries', 'Net Balance (PKR)'];
    const rows = (appState.customLedgersList || []).map(l => {
      const entries = l.entries || [];
      const totalDebit = entries.reduce((acc, e) => acc + (Number(e.debit) || 0), 0);
      const totalCredit = entries.reduce((acc, e) => acc + (Number(e.credit) || 0), 0);
      return [
        l.id || '',
        l.name || '',
        entries.length,
        Math.round(totalDebit - totalCredit)
      ];
    });
    return [headers, ...rows];
  }

  // 16. Inquiries
  if (norm === 'inquiries' || norm === 'inquiry log' || norm === 'leads') {
    const headers = ['Inquiry ID', 'Date', 'Party Name', 'Phone', 'Detail', 'Urgency', 'Resolved Status'];
    const rows = (appState.inquiries || []).map(i => [
      i.id || '',
      i.date || '',
      i.party || '',
      i.phone || '',
      (i.detail || '').replace(/[\r\n]+/g, ' '),
      i.urgency || 'normal',
      i.resolved ? 'RESOLVED' : 'OPEN'
    ]);
    return [headers, ...rows];
  }

  // 17. Gate Receipts / Passes
  if (norm === 'gate receipts' || norm === 'gate passes' || norm === 'gate registry' || norm === 'gate pass') {
    const headers = ['Gate Pass #', 'Source', 'Party / Factory', 'Date', 'Time', 'Received By', 'Gate Post', 'Item Details', 'Status'];
    const txPasses = (appState.transactions || [])
      .filter(t => t.gatePass?.gatePassNo || t.gateSequenceNo || t.receiptUrl)
      .map(t => [
        t.gatePass?.gatePassNo || t.gateSequenceNo || `GP-${t.id}`,
        'Factory Customer Order',
        t.factory || '',
        t.date || '',
        t.time || '',
        t.gateReceivedBy || t.gatePass?.receivedBy || 'Main Counter',
        t.gatePost || t.gatePass?.gatePost || 'Gate 1',
        (t.itemsSummary || '').replace(/[\r\n]+/g, ' '),
        t.paid ? 'PAID' : 'PENDING'
      ]);
    return [headers, ...txPasses];
  }

  // 18. Match Individual Custom Ledger by exact or lowercased name
  const matchedCustomLedger = (appState.customLedgersList || []).find(
    l => l.name.trim().toLowerCase() === norm || l.id.toLowerCase() === norm
  );
  if (matchedCustomLedger) {
    const headers = ['Date', 'Time', 'Description', 'Size', 'Qty', 'Rate (PKR)', 'Debit (PKR)', 'Credit (PKR)', 'Method', 'Received By'];
    const rows = (matchedCustomLedger.entries || []).map(e => [
      e.date || '',
      e.time || '',
      (e.desc || '').replace(/[\r\n]+/g, ' '),
      e.size || '',
      Number(e.qty) || '',
      Number(e.rate) || '',
      Number(e.debit) || 0,
      Number(e.credit) || 0,
      e.method || 'Cash',
      e.receivedBy || ''
    ]);
    return [headers, ...rows];
  }

  // Default fallback
  return [
    ['Sheet Name', 'Generated On', 'Item Count'],
    [sheetTitle, new Date().toLocaleString('en-GB'), '0']
  ];
}

/**
 * Clear and write complete values for a specific worksheet tab
 */
export async function writeSheetValues(
  accessToken: string,
  spreadsheetId: string,
  sheetTitle: string,
  values: (string | number)[][]
): Promise<number> {
  const cleanId = extractSpreadsheetId(spreadsheetId);

  // Ensure sheet exists before writing
  try {
    const info = await getSpreadsheetInfo(accessToken, cleanId);
    if (!info.sheets.some(s => s.title === sheetTitle)) {
      await fetch(`https://sheets.googleapis.com/v4/spreadsheets/${cleanId}:batchUpdate`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          requests: [{
            addSheet: {
              properties: {
                title: sheetTitle,
                gridProperties: { rowCount: 200, columnCount: 20, frozenRowCount: 1 }
              }
            }
          }]
        })
      });
    }
  } catch (err) {
    console.warn(`Non-blocking sheet tab creation check for "${sheetTitle}":`, err);
  }

  const range = `'${sheetTitle}'!A1`;

  // Clear existing content in sheet first
  await fetch(`https://sheets.googleapis.com/v4/spreadsheets/${cleanId}/values/'${sheetTitle}':clear`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    }
  });

  // Write new values
  const url = `https://sheets.googleapis.com/v4/spreadsheets/${cleanId}/values/${encodeURIComponent(range)}?valueInputOption=USER_ENTERED`;
  const response = await fetch(url, {
    method: 'PUT',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      range,
      majorDimension: 'ROWS',
      values
    })
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody?.error?.message || `Failed to write sheet "${sheetTitle}" (${response.status})`);
  }

  const result = await response.json();
  return result.updatedRows || values.length;
}

/**
 * Synchronize all applicable sheets with smart data generation
 */
export async function syncSmartSpreadsheet(
  accessToken: string,
  spreadsheetId: string,
  appState: AppState
): Promise<SheetSyncResult[]> {
  const tabs = getAllApplicableSheetTabs(appState);
  await ensureRequiredSheetsExist(accessToken, spreadsheetId);

  const results: SheetSyncResult[] = [];

  for (const tabTitle of tabs) {
    try {
      const values = buildTabValues(tabTitle, appState);
      const rowsUpdated = await writeSheetValues(accessToken, spreadsheetId, tabTitle, values);
      results.push({
        sheetName: tabTitle,
        rowsUpdated,
        status: 'success'
      });
    } catch (err: any) {
      results.push({
        sheetName: tabTitle,
        rowsUpdated: 0,
        status: 'error',
        error: err?.message || 'Failed to update sheet'
      });
    }
  }

  setLastSheetsSyncTime(new Date().toISOString());
  return results;
}

/**
 * Populate all 7 standard sheets in a master spreadsheet
 */
export async function populateAllSheets(
  accessToken: string,
  spreadsheetId: string,
  appState: AppState
): Promise<SheetSyncResult[]> {
  await ensureRequiredSheetsExist(accessToken, spreadsheetId);

  const syncTasks: { title: string; values: (string | number)[][] }[] = [
    { title: 'Dashboard', values: buildDashboardValues(appState) },
    { title: 'Sales Transactions', values: buildSalesTransactionsValues(appState.transactions || []) },
    { title: 'Product Catalog', values: buildProductCatalogValues(appState.products || []) },
    { title: 'Customer Ledgers', values: buildCustomerLedgerValues(appState.customerLedgers || []) },
    { title: 'Raw Material Stock', values: buildRawStockValues(appState.rawStock || []) },
    { title: 'Factory Production', values: buildFactoryProductionValues(appState.factories || [], appState.transactions || []) },
    { title: 'Workshop Expenses', values: buildExpensesValues(appState.expenses || []) }
  ];

  const results: SheetSyncResult[] = [];

  for (const task of syncTasks) {
    try {
      const rowsUpdated = await writeSheetValues(accessToken, spreadsheetId, task.title, task.values);
      results.push({
        sheetName: task.title,
        rowsUpdated,
        status: 'success'
      });
    } catch (err: any) {
      results.push({
        sheetName: task.title,
        rowsUpdated: 0,
        status: 'error',
        error: err?.message || 'Failed to update sheet'
      });
    }
  }

  setLastSheetsSyncTime(new Date().toISOString());
  return results;
}

/**
 * Read rows from any sheet in the spreadsheet for in-app preview
 */
export async function readSheetValues(
  accessToken: string,
  spreadsheetId: string,
  sheetTitle: string,
  rangeLimit: number = 30
): Promise<(string | number)[][]> {
  const cleanId = extractSpreadsheetId(spreadsheetId);
  const range = `'${sheetTitle}'!A1:Z${rangeLimit}`;
  const url = `https://sheets.googleapis.com/v4/spreadsheets/${cleanId}/values/${encodeURIComponent(range)}`;

  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${accessToken}`
    }
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody?.error?.message || `Failed to read sheet data (${response.status})`);
  }

  const data = await response.json();
  return data.values || [];
}

/**
 * Append a single new transaction row to the Sales Transactions sheet
 */
export async function appendTransactionToSheet(
  accessToken: string,
  spreadsheetId: string,
  transaction: Transaction
): Promise<void> {
  const cleanId = extractSpreadsheetId(spreadsheetId);
  const range = `'Sales Transactions'!A1`;
  const row = [
    transaction.id || '',
    transaction.date || '',
    transaction.time || '',
    transaction.factory || 'Walk-in Customer',
    Number(transaction.total) || 0,
    transaction.paid ? 'PAID' : 'PENDING',
    transaction.method || (transaction.detailCash ? 'Cash' : transaction.detailBank ? 'Bank Transfer' : 'Unspecified'),
    transaction.itemCount || 1,
    (transaction.itemsSummary || '').replace(/[\r\n]+/g, ' '),
    transaction.sizes || '',
    transaction.gateSequenceNo || (transaction.gateSequence ? `#${transaction.gateSequence}` : ''),
    transaction.gateReceivedBy || '',
    transaction.device || 'Counter Terminal'
  ];

  const url = `https://sheets.googleapis.com/v4/spreadsheets/${cleanId}/values/${encodeURIComponent(range)}:append?valueInputOption=USER_ENTERED&insertDataOption=INSERT_ROWS`;

  await fetch(url, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      values: [row]
    })
  });
}

/**
 * Quick refresh for just the Dashboard metrics worksheet tab
 */
export async function updateDashboardSheet(
  accessToken: string,
  spreadsheetId: string,
  appState: AppState
): Promise<number> {
  const values = buildDashboardValues(appState);
  const rows = await writeSheetValues(accessToken, spreadsheetId, 'Dashboard', values);
  const now = new Date().toLocaleString('en-GB');
  setLastSheetsSyncTime(now);
  return rows;
}

/**
 * Seamless single-transaction sync: Appends the transaction and updates dashboard metrics
 */
export async function syncSingleTransactionWithSheet(
  accessToken: string,
  spreadsheetId: string,
  transaction: Transaction,
  updatedAppState?: AppState
): Promise<void> {
  await appendTransactionToSheet(accessToken, spreadsheetId, transaction);
  if (updatedAppState) {
    try {
      await updateDashboardSheet(accessToken, spreadsheetId, updatedAppState);
    } catch (dashErr) {
      console.warn('Dashboard sheet update notice:', dashErr);
    }
  }
  const now = new Date().toLocaleString('en-GB');
  setLastSheetsSyncTime(now);
}

