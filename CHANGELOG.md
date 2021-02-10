## [1.3.3]
### Fixed
- [AM-2703][MCD] Android | MCD | Able to filter with 0 amount

## [1.3.2]
### Fixed
- [AM-2688][Tutorials] Tutorials in Transact tab points to a white space


## [1.3.1]
### Fixed
- [AM-2643] Account Details Screen] Encountering a Blank screen with error while clicking on transaction entries 
- [AM-2682] MCD | Android | Remarks field is missing on Summary page if No remarks was inputted
- [AM-2684] MCD | Android | Deposit to field is missing in Transaction Cards


## [1.3.0] based on prod version [1.2.3]
### Added
- [AM-1843]	As a Corporate User, I should receive an email notification on the status of the check once approved/rejected in Appian MCD module 
- [AM-2325]	Addition of Remarks Field in MCD Screens 
- [AM-2326]	Addition of Reference Number field in MCD Screens 
- [AM-2491]	As a client, I should be allowed to retake the ID and select a different ID type 

### Changed
- [AM-2492] Optimizations for Accounts in Beneficiaries 
- [AM-2519]	Optimization for Accounts in Frequent Biller 
- [AM-2276]	As a corporate user, I should see updated push notifications (Single FT) 
- [AM-2581]	As a corporate user, I should see updated push notifications (Batch FT) 
- [AM-2582]	As a corporate user, I should see updated push notifications (Bills Payment) 
- [AM-2496]	As a corporate client, I should only be able to receive push notifs from my trusted devices. 
- [AM-2380]	As a Client, I should be able to deposit checks to Penbank, 1st Valley Bank, Enterprise Bank 
- [AM-2322]	MCD UI Notification/Messages Adjustments 
- [AM-2391]	Update of field label in UI from "Check Date" to "Date in Front of Check" during transaction creation 
- [AM-2327] Update of Check Deposit Details fields to display Reason for Rejection
- [AM-2386]	As a corporate client, I should see the summary messages based from the API 
- [AM-2267]	As a corporate user, I should see the reason for the status in the details page (Single FT) 
- [AM-2579]	As a corporate user, I should see the reason for the status in the details page (Bills Payment) 
- [AM-2580]	As a corporate user, I should see the reason for the status in the details page (Batch FT) 
- [AM-2548]	Android & IOS | Able to submit FT to all channels with single Digit Bene account number

### Fixed
- [AM-2478]	Android | Password eye icon does not change upon tapping 
- [AM-2565]	Overlapping Alerts Header 
- [AM-2504]	Android | Nav menu Text should either resize to fit container/truncate instead of cutting off letters when user settings have a big font size selected. 
- [AM-2505]	Android | Account balances should either resize to fit container/truncate and not overlap w/ acct number when user settings have a big font size selected. 
- [AM-2513]	Android | Incorrect End Date Field label and value for Reoccurring Scheduled FT all channels 
- [AM-2635]	Android | <INC000000056159> "invalid fingerprint" upon login via app using fingerprint


## [1.2.10] based on prod version [1.2.2]
- [AM-2539] Bene Code should not allow spaces
- [AM-2548] Able to submit FT to all channels with single Digit account number
- [AM-2553] DAO UI Alignment - Dropdown Option Screen


## [1.2.9] based on prod version [1.2.2]
- Updated version of firebase crashlytics to collect production crash issues.


## [1.2.8] based on prod version [1.2.2]
- [AM-2532] User should receive push notifs for Portal and SME apps if same User is logged in for both apps
- [AM-2477] Error displayed then force closes upon tapping a transaction in Batch FTs


## [1.2.7] based on prod version [1.2.2]
### Fixed
- [AM-2531] Invalid Bank Code upon trying FT to PDDTS Adhoc
- [AM-2529] Intermittent issue - Nothing happens upon tapping Check Deposit
- Disabled ebilling product in transact screen


## [1.2.6] based on prod version [1.2.2]
### Changed
- [AM-2458]	Optimization for Accounts in Fund Transfer
- [AM-2459]	Optimization for Accounts in Bills Payment
- [AM-2460]	Optimization for Accounts in BTR
- [AM-2390]	Android | Login button remains disabled until 8 character/digit is entered
- [AM-1487]	No validation for changing of mobile number for Philippine numbers
- [AM-1630]	"Submit button for a transaction is not working after the tutorials are checked before submission of the FT Transactions (all channels)	"
- [AM-1631]	Scheduled FT tutorial is shown in the wrong field (all channels except ubp)
- [AM-2277]	As a corporate user, I want to view Cash Withdrawal transaction details in the Approvals tab
- [AM-2219]	Beneficiary Name should always be validated in PESONet and instaPay
- [AM-2196]	Filters End Date should be allowed to be inputted even without Start Date, tapping on the calendar icon does not triggers the date selector.
- [AM-2199]	Android | In FT & BP form, save button in transaction date should be in the upper right
- [AM-2191]	UI Alignment | FT & BP Channels Title
- [AM-2299]	To see batch breakdown, whole line should be clickable. Not just the small icon.
- [AM-2195]	Transfer Date should be updated to Transaction Date
- [AM-2197]	Groups in Approval Hierarchy is changing positions
- [AM-2203]	FT some FT fields should not have a separator "|" before the icon
- [AM-2376]	Change copies for SME Mobile App and in New Device Login Push Notif
- [AM-2283]	Change in Title in Dashboard
- [AM-2318]	As a corporate client, I want to see the new Union3C name and email address in both Portal and SME App
- [AM-2232]	DAO - Reference No. page should not be seen in the transition of T&C Page and Personal Information Page
- [AM-2231]	When a user have already filled in the forms without tapping next, then they go back, then tapped next again, user should *be prompted that their information will not be saved
- [AM-2309]	[VAPT] The Application should never expose the user's sensitive details like Reference number in the URL or API endpoints.
- [AM-2399]	Getting Error while performing signature verification
- [AM-2221]	DAO UI Alignment - Login Screen
- [AM-2222]	DAO UI Alignment - Modals
- [AM-2223]	DAO UI Alignment - Confirm Your Details page
- [AM-2224]	DAO UI Alignment - Upload Document Page
- [AM-2225]	DAO UI Alignment - Forms
- [AM-2226]	DAO UI Alignment - Dropdown Option Screen
- [AM-2227]	DAO UI Alignment - Terms & Condition Page
- [AM-2228]	DAO UI Alignment - Personal Info (Reference No) Page
- [AM-2261]	Revision of MCD push notification for redundant word "Check"
- [AM-2293]	MCD UI Field adjustments
- [AM-2265]	As a corporate user, I should be able to enter a Remarks value when doing check deposits.


## [1.2.5] based on prod version [1.2.0]
### Changed
- [AM-2383] Change PUT params into body for update signatory
- [AM-2332] The Application should never expose the user's sensitive details like Reference number in the URL or API endpoints.


## [1.2.4] based on prod version [1.2.0]
### Fixed
- [AM-2364] Incorrect Reject modal for FT and BP
- [AM-1872] Error for reason for cancellation should be in field instead of modal


## [1.2.3] based on prod version [1.2.0]
### Fixed
- [AM-2000] Error for reason for cancellation should be in field instead of modal 


## [1.2.2] based on prod version [1.2.0]
### Fixed
- [AM-1871] Update copy to "To notify the beneficiary, please enter their email or mobile number"
- [AM-1870] Update copy for reason for cancellation
- [AM-1872] Error for reason for cancellation should be in field instead of modal
- [AM-2364] Android | Incorrect Reject modal for FT and BP
- [AM-1700] As a corporate user, I should see a tool tip for the beneficiary code


## [1.2.1] based on prod version [1.2.0]
### Fixed
- [AM-1994] Wrong text copies for mcd guidelines


## [1.2.1] based on prod version [1.2.0]
### Fixed
- [AM-2200][Approvals] Cancel Transaction button is not present in the Transfer details with the Non-Maker user credentials. Only Maker of the scheduled Transfer, has the Cancel Transaction button
- [AM-2358][Transaction] Rejection reason field is missing in the Reject modal


## [1.2.0]
### Added
- [AM-1944][MCD] As a corporate user, I should be able to search for transactions previously created through portal mobile check deposit using a search bar
- [AM-2009][MCD] As a corporate user I should be able to use a search filter field in MCD
- [AM-2101][MCD] As a Portal MCD System, I should have a toggle on/off button in the back-end
- [AM-1952][FT/BP] Pass unique ref number to validate and avoid multiple posting
- [AM-2084][Accounts] As a corporate user, I should be able to click a transaction in the transaction history, so that i can view its complete details
- [AM-2116][FT] As a client, I should see the correct FT reminder based on DB
- [AM-2158][MCD] As a corporate user, I should be redirected to the check details page of the app upon clicking MCD push notification.
- [AM-1854][MCD] As a Corporate Client, I should be prompted to read the new T&Cs with full details for MCD.
- [AM-1700][Beneficiary] As a corporate user, I should see a tool tip for the beneficiary code
- [AM-1835][FT] As a corporate user, i should be able to indicate reason for cancellation for scheduled Fund Transfer
- [AM-2285][CDAO] toggle off the entire cDAO application


### Fixed
- [AM-2198][FT/BP] Android | Opening filters in FT and BP is prompting an error and closing the app
- [AM-2200][Approvals] Android | Rejection reason field is missing in the Reject modal
- [AM-2186][Organization] Android | Under Organization dropdown, the user's name should show instead of the Org name
- [AM-2230][Approvals] Scale the approval hierarchy properly in the Transaction details page
- [AM-2256][Settings] Android | When clicking on Security and notification ,Org Is getting auto logged out 
- [AM-2082][FT] Android | Incorrect Error message in Summary Page for InstaPay Insufficient Funds
- [AM-2085][Tutorials] Fix copy for tutorials in Business Banking App (SME and Portal)
- [AM-1787][MCD] Android | Allowing 0.00 amount section to be posted


### Changes
- [AM-1994][MCD] Updated MCD guidelines
- [AM-2206][Login] Android | I should be able to enter special characters on the login fields
- [AM-1891][MCD] As an MCD feature, the current status names should be updated
- [AM-2000][MCD] Addition of validation on MCD check amounts 500,000 and above.
- [AM-2196][Transaction Filters] Filters End Date should be allowed to be inputted even without Start Date, tapping on the calendar icon does not triggers the date selector.
- [AM-2146][MCD] As a Portal MCD System, I should not display the MCD transactions in the Website and App Alerts.
- [AM-2157][FT/BP] As a corporate user, I should see an error prompt for I don't have permission to create an FT and BP
- [AM-2160][Tutorials] Change copy to SME Business Banking instead of Portal
- [AM-2008][Approvals] The border around a group in the approval hierarchy should be the same color as the "You are here legend" #FA4616
- [AM-1872][Approvals] Error for reason for cancellation should be in field instead of modal
- [AM-1894][MCD] As a corporate user using MCD, I should not have to enter "check account name" in the Check Details
- [AM-1870][MCD] Update copy for reason for cancellation
- [AM-1871][Beneficiary] Update copy to "To notify the beneficiary, please enter their email or mobile number"
- [AM-1873][Forms] Info icon should be "i" instead of "!"
- [AM-1875][Approvals] Keyboard enter should be DONE instead
- [AM-1587][Frequent Biller] As a corporate user, I should see an updated icon for frequent biller.
- [AM-2199][Scheduled Transfer] In FT and BP forms, save button in transaction date should be on the top right
- [AM-2191][Channel] FT & BP Channels Title



## [1.1.9]
### Fixed
- As an MCD feature, update the "Transfer To" and "Transfer From" labels.
-  Mobile Check Deposit | Back Office Permissions | When "View Balances" & "View Transactions History" is unticked in the Back Office , the MCD Transactions does not allow the User to Choose a "Deposit To" account in order to post a check transaction.

### Changed
- Update of MCD guidelines for portal users.


## [1.1.8]
### Added
- Support push notif alert type MAINTENANCE and NEW_FEATURES
- Permission Id for getting accounts in mcd form.

### Fixed
- Crash when sharing transactions (for only android 10 above).
- 500 error when clicking bills payment push notif.
- Inconsistent account transaction history list.
- Switch organization dialog didn't show the YES action.
- Wrong display of Confirmation message on the bills payment successful screen. Its shows that the bills payment transaction are scheduled, even though the Payment Date field is Immediately.


## [1.1.7]
### Fixed
- Not receiving push notification when using tries use password authentication 
- Pagination issue in frequent biller

### Changed
- White push notif UB icon


## [1.1.6]

### Fixed
- Increase quality and pixels of images in MCD


## [1.1.5]

### Fixed
- SME theming


## [1.1.4]

### Added
- [Transaction Filter][FT/BP] New Transaction filter form with search function.
- [FT] Added pop up modal in FT list when batch files is ongoing it shoudnt be redirected to details.
- [Beneficiary] Added tool tip in beneficiary code.
- [Swift] Added tool tip in swift account number.
- [Settiings] Switching of ui between listview and cardview.
- [OTP] Resend OTP is now available in all screen required with OTP validation.
- [FT/BP] Added cancel transactions that are still pending for approval.
- [OTP] Change behavior of otp resend from unlimited resend every 100 seconds to once.
- [Approvals] Increase length of reason of rejection to 200 characters.
- [Mobile Check Deposit] MCD Feature under transact tab.
- [Dashboard] Full light version of SME
- [Network Security][VAPT] Use https and added SSL for network request.


### Changes
...


### Fixed
- [Dashboard] Fixed organization badge 0 value.
- [Beneficiary] Fixed beneficiary instapay code is required error when creating beneficiary.
- [FT/BP] Handling of new status when sharing the transaction.
- [Check Deposit] Align text description in check deposit reminders.
- [Check Deposit] Sorting of activity logs in check deposit details.
- [Beneficiary] Mobile number field is added in the create Beneficiary screen for UBP, PESONET, PDDTS, SWIFT & InstaPay channels but not in the Andorid platform.
- [Scheduled Transfer] Confirmation message label #2 verbiage mismatch issue between Andorid & IOS device.
- [Learn More] Verbiage Issue in Learn More Page - Word 'The' is repeated twice in the second Question of Learn More page.
- [Login] Forgot password link is not present in the Login screen.
- [Check Deposit] Fixed share content in transaction failed.
- [Approvals] Cancel pending approval alert message.
- [Check Writer] Fixed rejection in check writer transaction.