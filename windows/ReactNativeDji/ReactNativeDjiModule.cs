using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace React.Native.Dji.ReactNativeDji
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class ReactNativeDjiModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="ReactNativeDjiModule"/>.
        /// </summary>
        internal ReactNativeDjiModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "ReactNativeDji";
            }
        }
    }
}
