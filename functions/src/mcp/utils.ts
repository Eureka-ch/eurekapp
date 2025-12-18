// Co-authored by Claude Code
import * as admin from 'firebase-admin';

/** Error thrown when a requested resource is not found */
export class NotFoundError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'NotFoundError';
  }
}

/** Error thrown when user lacks permission to access a resource */
export class ForbiddenError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'ForbiddenError';
  }
}

export function serializeDoc(doc: admin.firestore.DocumentSnapshot): object {
  if (!doc.exists) return {};
  const data = doc.data()!;
  return serializeData({ id: doc.id, ...data });
}

export function serializeData(data: Record<string, unknown>): object {
  const result: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(data)) {
    if (value instanceof admin.firestore.Timestamp) {
      result[key] = value.toDate().toISOString();
    } else if (value && typeof value === 'object' && !Array.isArray(value)) {
      result[key] = serializeData(value as Record<string, unknown>);
    } else {
      result[key] = value;
    }
  }
  return result;
}
