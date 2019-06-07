import org.aspectj.lang.annotation.Pointcut;

public class SystemArchitecture {
	@Pointcut("execution(void com.aia.Repository.*.*(..))")
	public void Repository() {
	}

	@Pointcut("execution(void com.aia.Service.*.*(..))")
	public void Service() {
	}
}
