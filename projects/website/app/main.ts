import {bootstrap} from "angular2/platform/browser";
import {HTTP_PROVIDERS} from "angular2/http";
import {AppComponent} from "./app.component";
import {provide} from "angular2/core";
import {AppSettings} from "./config";

bootstrap(AppComponent, [
    HTTP_PROVIDERS,
    provide(Window, {useValue: window}),
    provide('SETTINGS', {useValue: AppSettings.settings})
]);
