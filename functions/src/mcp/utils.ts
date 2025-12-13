// Co-authored by Claude Code
import * as admin from 'firebase-admin';

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
