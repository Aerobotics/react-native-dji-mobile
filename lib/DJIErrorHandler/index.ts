
const DJIGimbalAttitudeActionErrorDomain = {
  name: 'DJIGimbalAttitudeActionErrorDomain',
  errors: {
    [-1]: {
      name: 'DJIGimbalAttitudeActionErrorUnknown',
      // description: ''
    },
    [100]: {
      name: 'DJIGimbalAttitudeActionErrorYawOutsideMaxCapabilities',
      description: 'Yaw value passed is beyond the max capabilities of the gimbal.',
    },
    [101]: {
      name: 'DJIGimbalAttitudeActionErrorPitchOutsideMaxCapabilities',
      description: 'Pitch value passed is beyond the max capabilities of the gimbal.',
    },
    [102]: {
      name: 'DJIGimbalAttitudeActionErrorCompletionTimeInvalid',
      description: 'Completion Time value passed is invalid (less than 0).',
    },
  },
};

const handledErrors = [
  DJIGimbalAttitudeActionErrorDomain,
];

const handledErrorNames = handledErrors.map(errorDomain => errorDomain.name);

export const handleSdkError = (err) => {
  let errorDomainIndex = -1;
  handledErrorNames.forEach((errorName, index) => {
    if (err.message.includes(errorName)) {
      errorDomainIndex = index;
    }
  });

  // if

};
