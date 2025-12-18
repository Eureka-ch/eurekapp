// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import {
  NotFoundError,
  ForbiddenError,
  serializeDoc,
  serializeData,
} from '../utils';

jest.mock('firebase-admin', () => {
  const mockTimestamp = class Timestamp {
    private _seconds: number;

    constructor(seconds: number, _nanoseconds: number) {
      this._seconds = seconds;
    }

    toDate() {
      return new Date(this._seconds * 1000);
    }
  };

  return {
    firestore: Object.assign(jest.fn(), {
      Timestamp: mockTimestamp,
    }),
  };
});

describe('utils', () => {
  describe('NotFoundError', () => {
    it('notFoundError_constructor_setsCorrectProperties', () => {
      const error = new NotFoundError('Resource not found');

      expect(error).toBeInstanceOf(Error);
      expect(error).toBeInstanceOf(NotFoundError);
      expect(error.message).toBe('Resource not found');
      expect(error.name).toBe('NotFoundError');
    });

    it('notFoundError_canBeCaughtAsError_succeeds', () => {
      const throwError = () => {
        throw new NotFoundError('Test error');
      };

      expect(throwError).toThrow(Error);
      expect(throwError).toThrow(NotFoundError);
    });
  });

  describe('ForbiddenError', () => {
    it('forbiddenError_constructor_setsCorrectProperties', () => {
      const error = new ForbiddenError('Access denied');

      expect(error).toBeInstanceOf(Error);
      expect(error).toBeInstanceOf(ForbiddenError);
      expect(error.message).toBe('Access denied');
      expect(error.name).toBe('ForbiddenError');
    });

    it('forbiddenError_canBeCaughtAsError_succeeds', () => {
      const throwError = () => {
        throw new ForbiddenError('Test error');
      };

      expect(throwError).toThrow(Error);
      expect(throwError).toThrow(ForbiddenError);
    });
  });

  describe('serializeDoc', () => {
    it('serializeDoc_existingDoc_returnsSerializedData', () => {
      const mockDoc = {
        exists: true,
        id: 'doc123',
        data: () => ({ name: 'Test', value: 42 }),
      } as unknown as admin.firestore.DocumentSnapshot;

      const result = serializeDoc(mockDoc);

      expect(result).toEqual({
        id: 'doc123',
        name: 'Test',
        value: 42,
      });
    });

    it('serializeDoc_nonExistingDoc_returnsEmptyObject', () => {
      const mockDoc = {
        exists: false,
        id: 'doc123',
        data: () => null,
      } as unknown as admin.firestore.DocumentSnapshot;

      const result = serializeDoc(mockDoc);

      expect(result).toEqual({});
    });
  });

  describe('serializeData', () => {
    it('serializeData_simpleObject_returnsUnchanged', () => {
      const data = { name: 'Test', count: 5, active: true };

      const result = serializeData(data);

      expect(result).toEqual(data);
    });

    it('serializeData_withTimestamp_convertsToISOString', () => {
      const timestamp = new admin.firestore.Timestamp(1700000000, 0);
      const data = { createdAt: timestamp, name: 'Test' };

      const result = serializeData(data);

      expect(result).toEqual({
        createdAt: '2023-11-14T22:13:20.000Z',
        name: 'Test',
      });
    });

    it('serializeData_withNestedObject_serializesRecursively', () => {
      const data = {
        name: 'Test',
        nested: {
          value: 42,
          deeper: {
            flag: true,
          },
        },
      };

      const result = serializeData(data);

      expect(result).toEqual(data);
    });

    it('serializeData_withArray_preservesArray', () => {
      const data = {
        items: [1, 2, 3],
        names: ['a', 'b'],
      };

      const result = serializeData(data);

      expect(result).toEqual(data);
    });

    it('serializeData_withNestedTimestamp_convertsRecursively', () => {
      const timestamp = new admin.firestore.Timestamp(1700000000, 0);
      const data = {
        name: 'Test',
        metadata: {
          updatedAt: timestamp,
        },
      };

      const result = serializeData(data);

      expect(result).toEqual({
        name: 'Test',
        metadata: {
          updatedAt: '2023-11-14T22:13:20.000Z',
        },
      });
    });

    it('serializeData_withNullValue_preservesNull', () => {
      const data = { name: 'Test', optional: null };

      const result = serializeData(data);

      expect(result).toEqual(data);
    });
  });
});
