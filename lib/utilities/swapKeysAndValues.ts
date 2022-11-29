interface Object {
  [key: string]: string
}

export const swapKeysAndValues = <T extends Object>(obj: T): Object => {
  const swapped = Object.entries(obj).map(
    ([key, value]) => [value, key]
  );
  return Object.fromEntries(swapped);
}
