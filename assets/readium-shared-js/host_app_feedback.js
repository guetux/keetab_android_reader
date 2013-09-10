//  LauncherOSX
//
//  Created by Boris Schneiderman.
//  Copyright (c) 2012-2013 The Readium Foundation.
//
//  The Readium SDK is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

//This is demo file that does nothing
//Host application has to override this file and provide it's own logic for handling ReadiumSDK events
//See LauncherOSX and LauncherIOS for examples
ReadiumSDK.HostAppFeedback = function() {

    ReadiumSDK.on("ReaderInitialized", function(){
        ReadiumSDK.reader.on("PaginationChanged", this.onPaginationChanged, this);
        ReadiumSDK.reader.on("SettingsApplied", this.onSettingsApplied, this);

    }, this);

    this.onPaginationChanged = function(paginationInfo) {
        if (window.LauncherUI) {
			window.LauncherUI.onPaginationChanged(JSON.stringify(paginationInfo));
        }

    };

    this.onSettingsApplied = function() {

        if(window.LauncherUI) {
            window.LauncherUI.onSettingsApplied();
        }
    };

}();

