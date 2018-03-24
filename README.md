# facebook-chat
Library, made to use facebook chat on android(SDK, java)

Logging in:

 Login mAuthTask = new Login(email, password);
 mAuthTask.execute(new Login.LoginCallback(){
        @Override
        public void fail() {
            //Handle fail
        }

        @Override
        public void success(Account ac) {
            //Handle success
        }

        @Override
        public void cancelled(){
            //Handle cancelled
        }
 });
