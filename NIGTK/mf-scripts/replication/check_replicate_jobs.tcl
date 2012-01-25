
set server [xvalue server/name [server.identity]]

set mail_from "mflux\@${server}"

set mail_to "mflux@pcomm.hfi.unimelb.edu.au"

foreach job [xvalues "job/@name" [schedule.job.describe]] {
    set err [ xvalue "job/replicate/error" [schedule.job.describe :job $job]]
    if { $err == "" } {
        set err [ xvalue "job/exec-error" [schedule.job.describe :job $job]]        
    }
    if { $err != "" } {
        set subject "Error occurs on replication job: ${job}"
        set body [xsafe [xelement job [schedule.job.describe :job $job]]]
        mail.send :from $mail_from :to $mail_to :subject $subject :body $body
    }
}
