import {bootstrap}    from 'angular2/platform/browser'
import {HTTP_PROVIDERS} from 'angular2/http'
import {AppComponent} from './app.component'
import {CompletionService} from './completion.service'
import {provide} from "angular2/core";

bootstrap(AppComponent, [
    HTTP_PROVIDERS,
    provide(CompletionService, {useClass: CompletionService})
]);
