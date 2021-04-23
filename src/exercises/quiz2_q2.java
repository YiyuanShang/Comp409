package exercises;

public class quiz2_q2 {
    class Complex{
        public double r, i;
    }

    class Parameter{
        Complex complex;

        public Parameter(double r, double i){
            complex.r = r;
            complex.i = i;
        }

        public Complex get(){
            return complex;
        }

        public void set(double newr, double newi){
           do{
                this.complex.r = newr;
                this.complex.i = newi;
           }while(!compareAndSet(this.complex, newr, newi));
        }

        private synchronized boolean compareAndSet(Complex x, double r, double i){
            if (x.r != r || x.i != i){
                return false;
            }
            this.complex.r = r;
            this.complex.i = i;
            return true;
        }
    }
}
