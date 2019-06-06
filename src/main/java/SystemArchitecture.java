import org.aspectj.lang.annotation.Pointcut;

public class SystemArchitecture {
	@Pointcut("execution(void (@org.springframework.stereotype.Repository *).*(..))")
	public void Repository() {
	}

	@Pointcut("execution(void (@org.springframework.stereotype.Service *).*(..))")
	public void Service() {
	}
}
