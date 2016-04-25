import {bootstrap} from "angular2/platform/browser";
import {HTTP_PROVIDERS} from "angular2/http";
import {ROUTER_PROVIDERS} from "angular2/router";
import {AppComponent} from "./app.component";
import {provide, enableProdMode} from "angular2/core";
import {AppSettings} from "./config-prod";
import {AngularFixService} from "./utils/angular-fix.service";

enableProdMode();

bootstrap(AppComponent, [
    HTTP_PROVIDERS,
    ROUTER_PROVIDERS,
    provide(Window, {useValue: window}),
    provide('SETTINGS', {useValue: AppSettings.settings}),
    provide(AngularFixService, {useClass: AngularFixService})
]);
