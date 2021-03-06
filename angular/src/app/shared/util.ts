import _uniq from 'lodash.uniq';
import { flatten } from "ddap-common-lib";

export function objectToArray(dto) {
  const bar = Object
    .keys(dto)
    .map((key) => {
      return {
        name: key,
        value: dto[key],
      };
    });

  return bar;
}

export function unique<T>(arrayOfArrays: T[][]): T[] {
  return _uniq(flatten(arrayOfArrays));
}

export function isEmptyObject(obj: object): boolean {
  return (obj && (Object.keys(obj).length === 0));
}

/**
 *
 * @param {text}
 * checks for only `://` in the string and returns true if present
 */
export function isUrl(text: string): boolean {
  return /(:\/{2})/.test(text);
}
