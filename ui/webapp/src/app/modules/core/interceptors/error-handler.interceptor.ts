import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NO_ERROR_HANDLING_HDR } from 'src/app/models/consts';
import { ConfigService } from '../services/config.service';
import { ErrorMessage, Logger, LoggingService } from '../services/logging.service';

@Injectable()
export class HttpErrorHandlerInterceptor implements HttpInterceptor {
  private log: Logger = this.loggingService.getLogger('HttpErrorHandlerInterceptor');

  constructor(private loggingService: LoggingService, private config: ConfigService, private snackbar: MatSnackBar, private router: Router) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((e) => {
        if (e instanceof HttpErrorResponse && !request.headers.has(NO_ERROR_HANDLING_HDR)) {
          switch (e.status) {
            case 0:
              this.config.checkServerReachable();
              return of(null);
            case 401:
              // let 401 pass through for logout redirection in the other interceptor :)
              break;
            case 403:
            // no break
            case 404:
              const msg = `Unfortunately, /${e.url} was not found (wrong URL or insufficient rights), we returned you to the safe-zone.`;
              this.snackbar.open(msg, 'DISMISS', { panelClass: 'error-snackbar' });
              this.router.navigate(['/groups/browser']);
              return of(null);
            case 499:
              // special version mismatch code.
              this.log.errorWithGuiMessage(new ErrorMessage(e.statusText, e));
              break;
            default:
              let displayPath = request.url;
              try {
                displayPath = new URL(request.url).pathname;
              } catch (error) {
                this.log.warn(new ErrorMessage('Cannot parse request URL', error));
              }
              this.log.errorWithGuiMessage(new ErrorMessage(e.status + ': ' + e.statusText + ': ' + displayPath, e));
              return of(null);
          }
        }
        return throwError(e);
      })
    );
  }
}
