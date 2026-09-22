import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';
import { getFirestore, doc, getDocFromServer, setLogLevel } from 'firebase/firestore';
import firebaseConfig from '../../firebase-applet-config.json';

// Silence verbose internal connection warnings during offline/reconnect cycles
setLogLevel('silent');

const app = initializeApp(firebaseConfig);
/* CRITICAL: The app will break without specifying firestoreDatabaseId */
export const db = getFirestore(
  app,
  (firebaseConfig as any).firestoreDatabaseId || 'ai-studio-falconrodmakerpo-4ec08e17-6c91-4aca-b59a-d747b503ca3a'
);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({
  prompt: 'select_account'
});

export enum OperationType {
  CREATE = 'create',
  UPDATE = 'update',
  DELETE = 'delete',
  LIST = 'list',
  GET = 'get',
  WRITE = 'write',
}

export interface FirestoreErrorInfo {
  error: string;
  operationType: OperationType;
  path: string | null;
  authInfo: {
    userId?: string | null;
    email?: string | null;
    emailVerified?: boolean | null;
    isAnonymous?: boolean | null;
    tenantId?: string | null;
    providerInfo?: {
      providerId?: string | null;
      email?: string | null;
    }[];
  };
}

export function handleFirestoreError(error: unknown, operationType: OperationType, path: string | null) {
  const errMsg = error instanceof Error ? error.message : String(error);
  
  // Gracefully handle expected offline or network disconnection without throwing fatal exceptions
  if (
    errMsg.toLowerCase().includes('unavailable') ||
    errMsg.toLowerCase().includes('client is offline') ||
    errMsg.toLowerCase().includes('could not reach cloud firestore')
  ) {
    console.info(`[Firestore Offline Cache] ${operationType} on ${path || 'database'}: Client operating with local persistence.`);
    return;
  }

  const errInfo: FirestoreErrorInfo = {
    error: errMsg,
    authInfo: {
      userId: auth.currentUser?.uid,
      email: auth.currentUser?.email,
      emailVerified: auth.currentUser?.emailVerified,
      isAnonymous: auth.currentUser?.isAnonymous,
      tenantId: auth.currentUser?.tenantId,
      providerInfo: auth.currentUser?.providerData?.map(provider => ({
        providerId: provider.providerId,
        email: provider.email,
      })) || []
    },
    operationType,
    path
  };
  console.error('Firestore Error: ', JSON.stringify(errInfo));
  throw new Error(JSON.stringify(errInfo));
}

export async function testConnection(): Promise<boolean> {
  try {
    const fetchPromise = getDocFromServer(doc(db, 'test', 'connection'));
    const timeoutPromise = new Promise<never>((_, reject) =>
      setTimeout(() => reject(new Error('connection check timed out')), 3500)
    );
    await Promise.race([fetchPromise, timeoutPromise]);
    return true;
  } catch (error) {
    if (error instanceof Error) {
      const msg = error.message.toLowerCase();
      if (
        msg.includes('offline') ||
        msg.includes('unavailable') ||
        msg.includes('failed to get document') ||
        msg.includes('could not reach') ||
        msg.includes('timed out')
      ) {
        // Handled: Offline storage active
        return false;
      } else {
        console.warn("Firestore connection check:", error.message);
      }
    }
    return false;
  }
}
