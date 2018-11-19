/*
 * Copyright (C) 2018  Clemens Bartz
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.clemensbartz.android.launcher.tasks;

import de.clemensbartz.android.launcher.Launcher;
import de.clemensbartz.android.launcher.models.ApplicationModel;
import de.clemensbartz.android.launcher.models.HomeModel;

/**
 * Toggle hidden field for app.
 * @author Clemens Bartz
 * @since 1.6
 */
public final class ToggleHiddenAsyncTask extends ToggleFieldAsyncTask {

    /**
     * Create a new task for toggling hiddenness of an app.
     * @param launcher the launcher
     * @param model the model
     */
    public ToggleHiddenAsyncTask(final Launcher launcher, final HomeModel model) {
        super(launcher, model);
    }

    @Override
    protected void toggleField(final HomeModel homeModel, final ApplicationModel applicationModel) {
        homeModel.toggleHidden(applicationModel.packageName, applicationModel.className);
    }
}