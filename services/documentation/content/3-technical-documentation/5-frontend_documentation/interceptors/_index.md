---
title: "Interceptors"
description: "Explaination of the interceptors roles"
date:  2023-12-28T08:20:38+01:00
weight: 20
---

The application G4IT uses two types of interceptors, one for the API calls and one for the errors.

### the API calls interceptor

You can find it in the `app/core/interceptors/api-request.interceptors.ts` directory.

His role is to intercept every call  from the frontend to the backend via the APIs. It will allow us to overwrite 
each of the url we call. 
The goal is to add in the url the user's subscriber and organization. It's simplifies the code and the management 
of the user's multi-organizations.

In this case, we subscribe once to the stored information on the user and we can adapt every call to the user's
subscriber and organization. It means that, if the user want to see another of his organization, the interceptor 
will be up-to-date thanks to the store. Moreover, he will only have access to the information of the chosen organization. 


### The errors interceptor

You can find it in the `app/core/interceptors/http-error.interceptors.ts` directory.

His role is to intercept every error returned from the backend on an API call. It will allow us to display a 
specifically designed web page for the error. The goal is to inform the user why the API call didn't work.

In this case, the error that are intercepted are:
- Forbidden
- Unauthorized
- Not found
- Bad request
- Request timeout
- Gateway timeout 
- Internal server error
- Service Unavailable

For each error, we have customized an error message to describe the problem that happened during the call. 
The interceptor redirects the user to the error page initialized in the error component in `app/layout/common/error`.
