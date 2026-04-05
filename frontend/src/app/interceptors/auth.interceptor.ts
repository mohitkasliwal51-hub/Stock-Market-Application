import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from 'src/environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('stock.jwt');
  const isApiRequest = req.url.startsWith(environment.apiURL);

  if (!token || !isApiRequest) {
    return next(req);
  }

  const cloned = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(cloned);
};
