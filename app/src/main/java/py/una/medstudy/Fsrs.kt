package py.una.medstudy

import java.util.concurrent.TimeUnit
import kotlin.math.*

// FSRS-5 default model at desired retention 0.90, with short learning steps.
object Fsrs {
    private val w=doubleArrayOf(0.40255,1.18385,3.173,15.69105,7.1949,0.5345,1.4604,0.0046,1.54575,0.1192,1.01925,1.9395,0.11,0.29605,2.2698,0.2315,2.9898,0.51655,0.6621)
    private const val day=86400000L
    private fun d0(g:Int)=(w[4]-exp(w[5]*(g-1))+1).coerceIn(1.0,10.0)
    fun next(p:Progress?,g:Int,now:Long):Progress {
        require(g in 1..4)
        val old=p ?: Progress("")
        val s:Double; val d:Double
        if(old.reviews==0){ s=w[g-1]; d=d0(g) }
        else {
            val previousS=old.stability.coerceAtLeast(0.01)
            val elapsed=((now-old.lastReview).coerceAtLeast(0L)/day.toDouble())
            val r=(1+19.0/81.0*elapsed/previousS).pow(-0.5)
            val delta=-w[6]*(g-3)
            d=(w[7]*d0(4)+(1-w[7])*(old.difficulty+delta*(10-old.difficulty)/9)).coerceIn(1.0,10.0)
            s=when {
                elapsed < 1.0 -> {
                    val candidate=previousS*exp(w[17]*(g-3+w[18]))
                    if(g==1) candidate else max(previousS,candidate)
                }
                g==1 -> w[11]*d.pow(-w[12])*((previousS+1).pow(w[13])-1)*exp(w[14]*(1-r))
                else -> previousS*(1+exp(w[8])*(11-d)*previousS.pow(-w[9])*(exp(w[10]*(1-r))-1)*(if(g==2) w[15] else 1.0)*(if(g==4) w[16] else 1.0))
            }
        }
        val stability=s.coerceIn(0.01,36500.0)
        val interval=(stability/(19.0/81.0)*(0.9.pow(-2.0)-1)).roundToLong().coerceIn(1,36500)
        val due=when(g){1->now+TimeUnit.MINUTES.toMillis(1);2->if(old.reviews==0) now+TimeUnit.MINUTES.toMillis(10) else now+interval*day;else->now+interval*day}
        return old.copy(stability=stability,difficulty=d,lastReview=now,due=due,reviews=old.reviews+1)
    }
}
