import React from "react";
import {browserHistory, Router, Route, DefaultRoute, Redirect } from "react-router";

import BaseLayout from "../components/layouts/Base";
import DashboardLayout from "../components/layouts/Dashboard";

import DashboardHomePage from "../components/pages/dashboard/Home";
import DashboardTablesPage from "../components/pages/dashboard/Tables";
import DashboardFormsPage from "../components/pages/dashboard/Forms";
import DashboardPanelsWellsPage from "../components/pages/dashboard/PanelsWells";
import DashboardButtonsPage from "../components/pages/dashboard/Buttons";
import DashboardNotificationsPage from "../components/pages/dashboard/Notifications";
import DashboardTypographyPage from "../components/pages/dashboard/Typography";
import DashboardIconsPage from "../components/pages/dashboard/Icons";
import DashboardGridPage from "../components/pages/dashboard/Grid";
import DashboardBlankPage from "../components/pages/dashboard/Blank";
import DashboardFlotChartsPage from "../components/pages/dashboard/FlotCharts";
import DashboardMorrisjsChartsPage from "../components/pages/dashboard/MorrisjsCharts";
import LoginPage from "../components/pages/Login";
import LogoutPage from "../components/pages/Logout";

var Routes = React.createClass({

  statics: {
    getRoutes: function() {
      return (
          <Route name="base" path="/" component={BaseLayout}>
            <Route name="dashboard" path="/dashboard" component={DashboardLayout}>
              <Route name="dashboard.home" path="/home" component={DashboardHomePage} />
              <Route name="dashboard.flot-charts" path="/flot-charts" component={DashboardFlotChartsPage} />
              <Route name="dashboard.morrisjs-charts" path="/morrisjs-charts" component={DashboardMorrisjsChartsPage} />
              <Route name="dashboard.tables" path="/tables" component={DashboardTablesPage} />
              <Route name="dashboard.forms" path="/forms" component={DashboardFormsPage} />
              <Route name="dashboard.panels-wells" path="/panels-wells" component={DashboardPanelsWellsPage} />
              <Route name="dashboard.buttons" path="/buttons" component={DashboardButtonsPage} />
              <Route name="dashboard.notifications" path="/notifications" component={DashboardNotificationsPage} />
              <Route name="dashboard.typography" path="/typography" component={DashboardTypographyPage} />
              <Route name="dashboard.icons" path="/icons" component={DashboardIconsPage} />
              <Route name="dashboard.grid" path="/grid" component={DashboardGridPage} />
              <Route name="dashboard.blank" path="/blank" component={DashboardBlankPage} />
              <DefaultRoute name="dashboard.default" component={DashboardHomePage} />
            </Route>
            <Route name="login" path="/login" component={LoginPage} />
            <Route name="logout" path="/logout" component={LogoutPage} />
            <DefaultRoute name="default" component={DashboardLayout} />
            <Redirect from="/" to="dashboard.home" />
          </Route>
      );
    }
  },
  render: function() {
  
  }
  
});

export default Routes;