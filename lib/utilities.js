// @flow strict

const usePromiseOrCallback = async (funcToCall: Function, callback: (err: ?string, response: any) => void, ...args: any) => {

  if (callback) {
    try {
      const response = await funcToCall(...args);
      callback(null, response);
    } catch (err) {
      callback(err, null);
    }
  }

  // Return the called function's promise
  return funcToCall(...args);
};

export {
  usePromiseOrCallback
};
